package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.ExpenseBalanceUpdateRequest;
import com.expenseshare.demo.dto.UserShareDto;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.entity.UserBalance;
import com.expenseshare.demo.repository.GroupRepository;
import com.expenseshare.demo.repository.UserBalanceRepository;
import com.expenseshare.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final UserBalanceRepository userBalanceRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    /**
     * Main method to update balances after expense creation
     */
    @Transactional
    public void updateBalancesForExpense(ExpenseBalanceUpdateRequest request) {
        log.info("Updating balances for expense - Group: {}, Paid by: {}, Amount: {}, Split Type: {}",
                request.getGroupId(), request.getPaidByUserId(), request.getTotalAmount(), request.getSplitType());

        validateExpenseBalanceRequest(request);

        Group group = getGroupById(request.getGroupId());
        User paidByUser = getUserById(request.getPaidByUserId());

        if (request.getSplitType().equalsIgnoreCase("EQUAL")) {
            processEqualSplit(group, paidByUser, request.getTotalAmount(), request.getGroupUserIds());
        } else if (request.getSplitType().equalsIgnoreCase("UNEQUAL")) {
            processUnequalSplit(group, paidByUser, request.getUserShares());
        }

        log.info("Balance update completed for expense");
    }

    /**
     * Process equal split among all group members
     */
    @Transactional
    public void processEqualSplit(Group group, User paidByUser, BigDecimal totalAmount, List<Long> userIds) {
        log.debug("Processing equal split for {} users", userIds.size());

        BigDecimal sharePerUser = totalAmount.divide(BigDecimal.valueOf(userIds.size()), 2, RoundingMode.HALF_UP);

        for (Long userId : userIds) {
            if (!userId.equals(paidByUser.getId())) {
                User user = getUserById(userId);
                // User owes sharePerUser to the person who paid
                updateBalance(group.getId(), userId, paidByUser.getId(), sharePerUser);

                log.debug("Equal split: User {} owes {} to User {}",
                        user.getUserName(), sharePerUser, paidByUser.getUserName());
            }
        }
    }

    /**
     * Process unequal split based on specified shares
     */
    @Transactional
    public void processUnequalSplit(Group group, User paidByUser, List<UserShareDto> userShares) {
        log.debug("Processing unequal split for {} users", userShares.size());

        for (UserShareDto userShare : userShares) {
            if (!userShare.getUserId().equals(paidByUser.getId())) {
                User user = getUserById(userShare.getUserId());
                // User owes their share to the person who paid
                updateBalance(group.getId(), userShare.getUserId(), paidByUser.getId(), userShare.getShare());

                log.debug("Unequal split: User {} owes {} to User {}",
                        user.getUserName(), userShare.getShare(), paidByUser.getUserName());
            }
        }
    }

    /**
     * Core method to update balance between two users in a group
     */
    @Transactional
    public void updateBalance(Long groupId, Long debtorId, Long creditorId, BigDecimal amount) {
        log.debug("Updating balance - Group: {}, Debtor: {}, Creditor: {}, Amount: {}",
                groupId, debtorId, creditorId, amount);

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return; // No need to update for zero amount
        }

        if (debtorId.equals(creditorId)) {
            return; // No balance update needed for same user
        }

        Group group = getGroupById(groupId);
        User debtor = getUserById(debtorId);
        User creditor = getUserById(creditorId);

        // Check if there's an existing opposite balance (creditor owes debtor)
        Optional<UserBalance> oppositeBalance = userBalanceRepository
                .findByGroupIdAndDebtorIdAndCreditorId(groupId, creditorId, debtorId);

        if (oppositeBalance.isPresent()) {
            // There's an opposite balance, so we need to net them out
            handleOppositeBalance(oppositeBalance.get(), amount, group, debtor, creditor);
        } else {
            // No opposite balance, create or update direct balance
            handleDirectBalance(groupId, debtorId, creditorId, amount, group, debtor, creditor);
        }

        log.debug("Balance updated successfully");
    }

    /**
     * Handle case where there's an existing opposite balance
     */
    private void handleOppositeBalance(UserBalance oppositeBalance, BigDecimal newAmount,
                                       Group group, User debtor, User creditor) {
        BigDecimal existingAmount = oppositeBalance.getAmount();

        if (newAmount.compareTo(existingAmount) > 0) {
            // New debt is larger, remove opposite balance and create new balance
            userBalanceRepository.delete(oppositeBalance);
            BigDecimal remainingAmount = newAmount.subtract(existingAmount);

            UserBalance newBalance = UserBalance.builder()
                    .group(group)
                    .debtor(debtor)
                    .creditor(creditor)
                    .amount(remainingAmount)
                    .build();
            userBalanceRepository.save(newBalance);

            log.debug("Opposite balance cleared, new balance created: {}", remainingAmount);

        } else if (newAmount.compareTo(existingAmount) < 0) {
            // Opposite balance is larger, just reduce it
            BigDecimal remainingAmount = existingAmount.subtract(newAmount);
            oppositeBalance.setAmount(remainingAmount);
            userBalanceRepository.save(oppositeBalance);

            log.debug("Opposite balance reduced from {} to {}", existingAmount, remainingAmount);

        } else {
            // Amounts are equal, they cancel out
            userBalanceRepository.delete(oppositeBalance);
            log.debug("Opposite balance exactly canceled out");
        }
    }

    /**
     * Handle direct balance (no opposite balance exists)
     */
    private void handleDirectBalance(Long groupId, Long debtorId, Long creditorId, BigDecimal amount,
                                     Group group, User debtor, User creditor) {
        // Find existing direct balance
        Optional<UserBalance> existingBalance = userBalanceRepository
                .findByGroupIdAndDebtorIdAndCreditorId(groupId, debtorId, creditorId);

        if (existingBalance.isPresent()) {
            // Update existing balance
            UserBalance balance = existingBalance.get();
            BigDecimal oldAmount = balance.getAmount();
            BigDecimal newAmount = oldAmount.add(amount);
            balance.setAmount(newAmount);

            if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
                userBalanceRepository.delete(balance);
                log.debug("Balance became zero/negative, deleted. Was: {}", oldAmount);
            } else {
                userBalanceRepository.save(balance);
                log.debug("Balance updated from {} to {}", oldAmount, newAmount);
            }
        } else {
            // Create new balance if amount is positive
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                UserBalance newBalance = UserBalance.builder()
                        .group(group)
                        .debtor(debtor)
                        .creditor(creditor)
                        .amount(amount)
                        .build();
                userBalanceRepository.save(newBalance);
                log.debug("New balance created: {}", amount);
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     */
    @Transactional
    public void processExpenseShare(Long groupId, Long paidByUserId, Long userId, BigDecimal shareAmount) {
        if (!paidByUserId.equals(userId)) {
            // User owes money to the person who paid
            updateBalance(groupId, userId, paidByUserId, shareAmount);
        }
    }

    /**
     * Simplify balance calculations by consolidating small amounts
     */
    @Transactional
    public void simplifyBalances(Long groupId) {
        log.info("Simplifying balances for group: {}", groupId);

        List<UserBalance> groupBalances = userBalanceRepository.findByGroupId(groupId);

        // Remove balances less than 0.01
        groupBalances.stream()
                .filter(balance -> balance.getAmount().compareTo(new BigDecimal("0.01")) < 0)
                .forEach(balance -> {
                    log.debug("Removing small balance: {} owes {} to {}",
                            balance.getDebtor().getUserName(), balance.getAmount(), balance.getCreditor().getUserName());
                    userBalanceRepository.delete(balance);
                });
    }

    private void validateExpenseBalanceRequest(ExpenseBalanceUpdateRequest request) {
        if (request.getGroupId() == null) {
            throw new IllegalArgumentException("Group ID is required");
        }

        if (request.getPaidByUserId() == null) {
            throw new IllegalArgumentException("Paid by user ID is required");
        }

        if (request.getTotalAmount() == null || request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than zero");
        }

        if (request.getSplitType() == null || request.getSplitType().trim().isEmpty()) {
            throw new IllegalArgumentException("Split type is required");
        }

        if (request.getSplitType().equalsIgnoreCase("EQUAL")) {
            if (request.getGroupUserIds() == null || request.getGroupUserIds().isEmpty()) {
                throw new IllegalArgumentException("Group user IDs are required for equal split");
            }
        } else if (request.getSplitType().equalsIgnoreCase("UNEQUAL")) {
            if (request.getUserShares() == null || request.getUserShares().isEmpty()) {
                throw new IllegalArgumentException("User shares are required for unequal split");
            }

            // Validate that shares sum up to total amount
            BigDecimal totalShares = request.getUserShares().stream()
                    .map(UserShareDto::getShare)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalShares.compareTo(request.getTotalAmount()) != 0) {
                throw new IllegalArgumentException("Sum of user shares must equal total amount");
            }
        }
    }

    private Group getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}


