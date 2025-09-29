package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.*;
import com.expenseshare.demo.entity.*;
import com.expenseshare.demo.enums.SettlementStatus;
import com.expenseshare.demo.exception.*;
import com.expenseshare.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(value = {OptimisticLockingFailureException.class, ConcurrentSettlementException.class},
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public SettlementResponseDto settleBalance(SettleBalanceRequestDto request) {
        log.info("Processing settlement: Payer={}, Receiver={}, Amount={}",
                request.getPayerId(), request.getReceiverId(), request.getAmount());

        try {
            // Validate request
            validateSettlementRequest(request);

            // Fetch users
            User payer = getUserById(request.getPayerId());
            User receiver = getUserById(request.getReceiverId());

            // Find all groups where both users are members
            List<Group> commonGroups = groupRepository.findGroupsWithBothUsers(
                    request.getPayerId(), request.getReceiverId());

            if (commonGroups.isEmpty()) {
                throw new InvalidSettlementException(
                        "Users are not members of any common groups");
            }

            // Calculate total amount that payer owes to receiver across all groups
            BigDecimal totalOwed = calculateTotalOwed(request.getPayerId(), request.getReceiverId());

            if (totalOwed.compareTo(BigDecimal.ZERO) <= 0 ) {
                throw new InsufficientBalanceException(
                        "No outstanding balance found between users");
            }
        //Handling excess settlement amount
        if(request.getAmount().compareTo(totalOwed) > 0 ){
            throw new GenericException(
                    "Excess settlement amount not supported");
        }
            // Ensure settlement amount doesn't exceed what's owed
            BigDecimal settlementAmount = request.getAmount().min(totalOwed);

            // Create settlement record
            Settlement settlement = Settlement.builder()
                    .payer(payer)
                    .receiver(receiver)
                    .amount(settlementAmount)
                    .description(request.getDescription())
                    .status(SettlementStatus.COMPLETED)
                    .settlementDate(LocalDateTime.now())
                    .build();

            settlement = settlementRepository.save(settlement);

            // Process settlement across all groups - THIS IS THE CRITICAL ATOMIC OPERATION
            List<SettlementDetail> settlementDetails = processSettlementAcrossGroups(
                    settlement, commonGroups, settlementAmount);

            settlement.setSettlementDetails(settlementDetails);

            log.info("Settlement completed successfully. Settlement ID: {}", settlement.getId());

            return mapToSettlementResponse(settlement);

        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure during settlement, will retry", e);
            throw new ConcurrentSettlementException("Concurrent settlement detected, please retry");
        } catch (Exception e) {
            log.error("Settlement failed", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public UserBalanceResponseDto getUserBalance(Long userId) {
        log.info("Fetching user balance for user ID: {}", userId);

        User user = getUserById(userId);
        List<UserBalance> balances = userBalanceRepository.findAllBalancesForUser(userId);

        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalOwedBy = BigDecimal.ZERO;

        // Group balances by group
        Map<Long, List<UserBalance>> balancesByGroup = balances.stream()
                .filter(balance -> balance.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(balance -> balance.getGroup().getId()));

        List<GroupBalanceDetailDto> groupBalances = new ArrayList<>();

        for (Map.Entry<Long, List<UserBalance>> entry : balancesByGroup.entrySet()) {
            Group group = entry.getValue().get(0).getGroup();
            List<UserBalance> groupBalanceList = entry.getValue();

            List<IndividualBalanceDto> individualBalanceDtos = new ArrayList<>();

            for (UserBalance balance : groupBalanceList) {
                if (balance.getDebtor().getId().equals(userId)) {
                    // User owes money
                    totalOwed = totalOwed.add(balance.getAmount());
                    individualBalanceDtos.add(IndividualBalanceDto.builder()
                            .otherUserId(balance.getCreditor().getId())
                            .otherUserName(balance.getCreditor().getUserName())
                            .amount(balance.getAmount())
                            .build());
                } else if (balance.getCreditor().getId().equals(userId)) {
                    // User is owed money
                    totalOwedBy = totalOwedBy.add(balance.getAmount());
                    individualBalanceDtos.add(IndividualBalanceDto.builder()
                            .otherUserId(balance.getDebtor().getId())
                            .otherUserName(balance.getDebtor().getUserName())
                            .amount(balance.getAmount().negate()) // Negative indicates they owe you
                            .build());
                }
            }

            if (!individualBalanceDtos.isEmpty()) {
                groupBalances.add(GroupBalanceDetailDto.builder()
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .balances(individualBalanceDtos)
                        .build());
            }
        }

        return UserBalanceResponseDto.builder()
                .userId(user.getId())
                .userName(user.getUserName())
                .totalOwed(totalOwed)
                .totalOwedBy(totalOwedBy)
                .netBalance(totalOwed.subtract(totalOwedBy))
                .groupBalances(groupBalances)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SettlementResponseDto> getUserSettlements(Long userId) {
        log.info("Fetching settlements for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        List<Settlement> settlements = settlementRepository.findSettlementsByUser(userId);

        return settlements.stream()
                .map(this::mapToSettlementResponse)
                .collect(Collectors.toList());
    }

    private void validateSettlementRequest(SettleBalanceRequestDto request) {
        if (request.getPayerId() == null) {
            throw new InvalidSettlementException("Payer ID is required");
        }

        if (request.getReceiverId() == null) {
            throw new InvalidSettlementException("Receiver ID is required");
        }

        if (request.getPayerId().equals(request.getReceiverId())) {
            throw new InvalidSettlementException("Payer and receiver cannot be the same");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidSettlementException("Settlement amount must be greater than zero");
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private BigDecimal calculateTotalOwed(Long payerId, Long receiverId) {
        List<Long> groupIds = userBalanceRepository.findGroupsWithBalancesBetweenUsers(payerId, receiverId);

        BigDecimal totalOwed = BigDecimal.ZERO;

        for (Long groupId : groupIds) {
            List<UserBalance> balances = userBalanceRepository
                    .findBalancesBetweenUsersInGroupForUpdate(payerId, receiverId, groupId);

            for (UserBalance balance : balances) {
                if (balance.getDebtor().getId().equals(payerId) &&
                        balance.getCreditor().getId().equals(receiverId)) {
                    totalOwed = totalOwed.add(balance.getAmount());
                }
            }
        }

        return totalOwed;
    }

    // CRITICAL METHOD: Handles the atomic debit/credit operation
    private List<SettlementDetail> processSettlementAcrossGroups(
            Settlement settlement, List<Group> commonGroups, BigDecimal remainingAmount) {

        List<SettlementDetail> settlementDetails = new ArrayList<>();
        BigDecimal amountToSettle = remainingAmount;

        for (Group group : commonGroups) {
            if (amountToSettle.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // Get balances for this group with pessimistic lock to prevent concurrent modifications
            List<UserBalance> balances = userBalanceRepository
                    .findBalancesBetweenUsersInGroupForUpdate(
                            settlement.getPayer().getId(),
                            settlement.getReceiver().getId(),
                            group.getId());

            for (UserBalance balance : balances) {
                if (amountToSettle.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                // Only process if payer owes money to receiver
                if (balance.getDebtor().getId().equals(settlement.getPayer().getId()) &&
                        balance.getCreditor().getId().equals(settlement.getReceiver().getId())) {

                    BigDecimal balanceBefore = balance.getAmount();
                    BigDecimal settlementForThisBalance = amountToSettle.min(balanceBefore);
                    BigDecimal balanceAfter = balanceBefore.subtract(settlementForThisBalance);

                    // ATOMIC UPDATE: This is where the debit (payer) and credit (receiver) happens
                    // The balance represents what payer owes to receiver
                    // Reducing this balance = debiting payer + crediting receiver
                    balance.setAmount(balanceAfter);

                    // If balance becomes zero, we could delete it, but keeping for audit trail
                    userBalanceRepository.save(balance);

                    // Create settlement detail for audit trail
                    SettlementDetail detail = SettlementDetail.builder()
                            .settlement(settlement)
                            .group(group)
                            .amountSettled(settlementForThisBalance)
                            .balanceBefore(balanceBefore)
                            .balanceAfter(balanceAfter)
                            .build();

                    settlementDetails.add(settlementDetailRepository.save(detail));

                    amountToSettle = amountToSettle.subtract(settlementForThisBalance);

                    log.debug("SETTLEMENT PROCESSED: Group={}, Payer={} paid {} to Receiver={}. Balance: {} -> {}",
                            group.getName(), settlement.getPayer().getUserName(), settlementForThisBalance,
                            settlement.getReceiver().getUserName(), balanceBefore, balanceAfter);
                }
            }
        }

        return settlementDetails;
    }

    public SettlementResponseDto mapToSettlementResponse(Settlement settlement) {
        List<SettlementGroupDetailDto> groupDetails = settlement.getSettlementDetails().stream()
                .map(detail -> SettlementGroupDetailDto.builder()
                        .groupId(Objects.nonNull(detail.getGroup()) ? detail.getGroup().getId() : 0L)
                        .groupName(Objects.nonNull(detail.getGroup()) ? detail.getGroup().getName() : "")
                        .amountSettled(detail.getAmountSettled())
                        .balanceBefore(detail.getBalanceBefore())
                        .balanceAfter(detail.getBalanceAfter())
                        .build())
                .collect(Collectors.toList());

        return SettlementResponseDto.builder()
                .settlementId(settlement.getId())
                .payerId(settlement.getPayer().getId())
                .payerName(settlement.getPayer().getUserName())
                .receiverId(settlement.getReceiver().getId())
                .receiverName(settlement.getReceiver().getUserName())
                .totalAmountSettled(settlement.getAmount())
                .status(settlement.getStatus().name())
                .description(settlement.getDescription())
                .settlementDate(settlement.getSettlementDate())
                .groupDetails(groupDetails)
                .build();
    }

    public  List<Settlement> findSettlementsBetweenUsers(Long userId1, Long userId2){
        return settlementRepository.findSettlementsBetweenUsers(userId1, userId2);
    }
}

