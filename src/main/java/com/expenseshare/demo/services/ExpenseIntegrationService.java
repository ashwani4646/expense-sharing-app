package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.ExpenseBalanceUpdateRequest;
import com.expenseshare.demo.dto.UserShareDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to integrate with external Expense Service
 * This service provides methods that can be called when expenses are created/updated
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseIntegrationService {

    private final BalanceService balanceService;

    /**
     * Called when a new expense is created - updates all relevant balances
     */
    @Transactional
    public void handleExpenseCreated(Long expenseId, Long groupId, Long paidByUserId,
                                     BigDecimal totalAmount, String splitType,
                                     List<Long> groupUserIds, List<UserShareDto> userShares,
                                     String description) {

        log.info("Handling expense creation - ID: {}, Group: {}, Amount: {}",
                expenseId, groupId, totalAmount);

        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .expenseId(expenseId)
                .groupId(groupId)
                .paidByUserId(paidByUserId)
                .totalAmount(totalAmount)
                .splitType(splitType)
                .groupUserIds(groupUserIds)
                .userShares(userShares)
                .description(description)
                .build();

        balanceService.updateBalancesForExpense(request);

        log.info("Successfully updated balances for expense ID: {}", expenseId);
    }

    /**
     * Called when an expense is deleted - reverses the balance changes
     */
    @Transactional
    public void handleExpenseDeleted(Long expenseId, Long groupId, Long paidByUserId,
                                     BigDecimal totalAmount, String splitType,
                                     List<Long> groupUserIds, List<UserShareDto> userShares) {

        log.info("Handling expense deletion - ID: {}, Group: {}, Amount: {}",
                expenseId, groupId, totalAmount);

        // Create reverse request (negative amounts to reverse the original transaction)
        List<UserShareDto> reverseShares = null;
        if (userShares != null) {
            reverseShares = userShares.stream()
                    .map(share -> UserShareDto.builder()
                            .userId(share.getUserId())
                            .share(share.getShare().negate())
                            .build())
                    .collect(Collectors.toList());
        }

        ExpenseBalanceUpdateRequest reverseRequest = ExpenseBalanceUpdateRequest.builder()
                .expenseId(expenseId)
                .groupId(groupId)
                .paidByUserId(paidByUserId)
                .totalAmount(totalAmount.negate()) // Negative amount to reverse
                .splitType(splitType)
                .groupUserIds(groupUserIds)
                .userShares(reverseShares)
                .description("Reversal for deleted expense")
                .build();

        balanceService.updateBalancesForExpense(reverseRequest);

        log.info("Successfully reversed balances for deleted expense ID: {}", expenseId);
    }

    /**
     * Convenience method for equal split expense
     */
    @Transactional
    public void handleEqualSplitExpense(Long expenseId, Long groupId, Long paidByUserId,
                                        BigDecimal totalAmount, List<Long> groupUserIds,
                                        String description) {

        handleExpenseCreated(expenseId, groupId, paidByUserId, totalAmount,
                "EQUAL", groupUserIds, null, description);
    }

    /**
     * Convenience method for unequal split expense
     */
    @Transactional
    public void handleUnequalSplitExpense(Long expenseId, Long groupId, Long paidByUserId,
                                          BigDecimal totalAmount, List<UserShareDto> userShares,
                                          String description) {

        handleExpenseCreated(expenseId, groupId, paidByUserId, totalAmount,
                "UNEQUAL", null, userShares, description);
    }
}

