package com.expenseshare.demo;

import com.expenseshare.demo.dto.ExpenseBalanceUpdateRequest;
import com.expenseshare.demo.dto.UserShareDto;
import com.expenseshare.demo.services.BalanceService;
import com.expenseshare.demo.services.ExpenseIntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseIntegrationServiceTest {

    @Mock
    private BalanceService balanceService;
   
    @InjectMocks
    private ExpenseIntegrationService expenseIntegrationService;
   
    private List<Long> groupUserIds;
    private List<UserShareDto> userShares;

    @BeforeEach
    void setUp() {
        groupUserIds = Arrays.asList(1L, 2L, 3L);
       
        userShares = Arrays.asList(
                UserShareDto.builder().userId(1L).share(new BigDecimal("10.00")).build(),
                UserShareDto.builder().userId(2L).share(new BigDecimal("15.00")).build(),
                UserShareDto.builder().userId(3L).share(new BigDecimal("5.00")).build()
        );
    }

    @Test
    void testHandleExpenseCreated_EqualSplit_Success() {
        // Arrange
        Long expenseId = 1L;
        Long groupId = 1L;
        Long paidByUserId = 1L;
        BigDecimal totalAmount = new BigDecimal("30.00");
        String splitType = "EQUAL";
        String description = "Test expense";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseCreated(expenseId, groupId, paidByUserId,
                totalAmount, splitType, groupUserIds, null, description);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getExpenseId().equals(expenseId) &&
            request.getGroupId().equals(groupId) &&
            request.getPaidByUserId().equals(paidByUserId) &&
            request.getTotalAmount().equals(totalAmount) &&
            request.getSplitType().equals(splitType) &&
            request.getGroupUserIds().equals(groupUserIds) &&
            request.getUserShares() == null &&
            request.getDescription().equals(description)
        ));
    }

    @Test
    void testHandleExpenseCreated_UnequalSplit_Success() {
        // Arrange
        Long expenseId = 2L;
        Long groupId = 2L;
        Long paidByUserId = 2L;
        BigDecimal totalAmount = new BigDecimal("30.00");
        String splitType = "UNEQUAL";
        String description = "Unequal expense";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseCreated(expenseId, groupId, paidByUserId,
                totalAmount, splitType, null, userShares, description);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getExpenseId().equals(expenseId) &&
            request.getGroupId().equals(groupId) &&
            request.getPaidByUserId().equals(paidByUserId) &&
            request.getTotalAmount().equals(totalAmount) &&
            request.getSplitType().equals(splitType) &&
            request.getGroupUserIds() == null &&
            request.getUserShares().equals(userShares) &&
            request.getDescription().equals(description)
        ));
    }

    @Test
    void testHandleExpenseDeleted_EqualSplit_Success() {
        // Arrange
        Long expenseId = 1L;
        Long groupId = 1L;
        Long paidByUserId = 1L;
        BigDecimal totalAmount = new BigDecimal("30.00");
        String splitType = "EQUAL";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseDeleted(expenseId, groupId, paidByUserId,
                totalAmount, splitType, groupUserIds, null);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getExpenseId().equals(expenseId) &&
            request.getGroupId().equals(groupId) &&
            request.getPaidByUserId().equals(paidByUserId) &&
            request.getTotalAmount().equals(totalAmount.negate()) &&
            request.getSplitType().equals(splitType) &&
            request.getGroupUserIds().equals(groupUserIds) &&
            request.getUserShares() == null &&
            request.getDescription().equals("Reversal for deleted expense")
        ));
    }

    @Test
    void testHandleExpenseDeleted_UnequalSplit_Success() {
        // Arrange
        Long expenseId = 2L;
        Long groupId = 2L;
        Long paidByUserId = 2L;
        BigDecimal totalAmount = new BigDecimal("30.00");
        String splitType = "UNEQUAL";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseDeleted(expenseId, groupId, paidByUserId,
                totalAmount, splitType, null, userShares);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request -> {
            boolean basicFieldsCorrect = request.getExpenseId().equals(expenseId) &&
                    request.getGroupId().equals(groupId) &&
                    request.getPaidByUserId().equals(paidByUserId) &&
                    request.getTotalAmount().equals(totalAmount.negate()) &&
                    request.getSplitType().equals(splitType) &&
                    request.getGroupUserIds() == null &&
                    request.getDescription().equals("Reversal for deleted expense");
           
            // Check if user shares are negated
            boolean sharesNegated = request.getUserShares() != null &&
                    request.getUserShares().size() == userShares.size() &&
                    request.getUserShares().stream().allMatch(share ->
                        userShares.stream().anyMatch(originalShare ->
                            originalShare.getUserId().equals(share.getUserId()) &&
                            originalShare.getShare().negate().equals(share.getShare())
                        )
                    );
           
            return basicFieldsCorrect && sharesNegated;
        }));
    }

    @Test
    void testHandleExpenseDeleted_UnequalSplit_NullUserShares_Success() {
        // Arrange
        Long expenseId = 3L;
        Long groupId = 3L;
        Long paidByUserId = 3L;
        BigDecimal totalAmount = new BigDecimal("50.00");
        String splitType = "UNEQUAL";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseDeleted(expenseId, groupId, paidByUserId,
                totalAmount, splitType, null, null);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getExpenseId().equals(expenseId) &&
            request.getGroupId().equals(groupId) &&
            request.getPaidByUserId().equals(paidByUserId) &&
            request.getTotalAmount().equals(totalAmount.negate()) &&
            request.getSplitType().equals(splitType) &&
            request.getGroupUserIds() == null &&
            request.getUserShares() == null &&
            request.getDescription().equals("Reversal for deleted expense")
        ));
    }

    @Test
    void testHandleEqualSplitExpense_Success() {
        // Arrange
        Long expenseId = 4L;
        Long groupId = 4L;
        Long paidByUserId = 4L;
        BigDecimal totalAmount = new BigDecimal("60.00");
        String description = "Equal split expense";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleEqualSplitExpense(expenseId, groupId, paidByUserId,
                totalAmount, groupUserIds, description);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getExpenseId().equals(expenseId) &&
            request.getGroupId().equals(groupId) &&
            request.getPaidByUserId().equals(paidByUserId) &&
            request.getTotalAmount().equals(totalAmount) &&
            request.getSplitType().equals("EQUAL") &&
            request.getGroupUserIds().equals(groupUserIds) &&
            request.getUserShares() == null &&
            request.getDescription().equals(description)
        ));
    }

    @Test
    void testHandleUnequalSplitExpense_Success() {
        // Arrange
        Long expenseId = 5L;
        Long groupId = 5L;
        Long paidByUserId = 5L;
        BigDecimal totalAmount = new BigDecimal("30.00");
        String description = "Unequal split expense";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleUnequalSplitExpense(expenseId, groupId, paidByUserId,
                totalAmount, userShares, description);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getExpenseId().equals(expenseId) &&
            request.getGroupId().equals(groupId) &&
            request.getPaidByUserId().equals(paidByUserId) &&
            request.getTotalAmount().equals(totalAmount) &&
            request.getSplitType().equals("UNEQUAL") &&
            request.getGroupUserIds() == null &&
            request.getUserShares().equals(userShares) &&
            request.getDescription().equals(description)
        ));
    }

    @Test
    void testHandleExpenseCreated_BalanceServiceThrowsException_PropagatesException() {
        // Arrange
        Long expenseId = 6L;
        Long groupId = 6L;
        Long paidByUserId = 6L;
        BigDecimal totalAmount = new BigDecimal("100.00");
        String splitType = "EQUAL";
        String description = "Test expense";
       
        RuntimeException exception = new RuntimeException("Balance service error");
        doThrow(exception).when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () ->
            expenseIntegrationService.handleExpenseCreated(expenseId, groupId, paidByUserId,
                    totalAmount, splitType, groupUserIds, null, description)
        );

        assertEquals("Balance service error", thrownException.getMessage());
        verify(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));
    }

    @Test
    void testHandleExpenseDeleted_BalanceServiceThrowsException_PropagatesException() {
        // Arrange
        Long expenseId = 7L;
        Long groupId = 7L;
        Long paidByUserId = 7L;
        BigDecimal totalAmount = new BigDecimal("75.00");
        String splitType = "UNEQUAL";
       
        RuntimeException exception = new RuntimeException("Balance service error during deletion");
        doThrow(exception).when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act & Assert
        RuntimeException thrownException = assertThrows(RuntimeException.class, () ->
            expenseIntegrationService.handleExpenseDeleted(expenseId, groupId, paidByUserId,
                    totalAmount, splitType, null, userShares)
        );

        assertEquals("Balance service error during deletion", thrownException.getMessage());
        verify(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));
    }

    @Test
    void testHandleExpenseCreated_WithNullValues_HandlesGracefully() {
        // Arrange
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseCreated(null, null, null,
                null, null, null, null, null);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getExpenseId() == null &&
            request.getGroupId() == null &&
            request.getPaidByUserId() == null &&
            request.getTotalAmount() == null &&
            request.getSplitType() == null &&
            request.getGroupUserIds() == null &&
            request.getUserShares() == null &&
            request.getDescription() == null
        ));
    }

    @Test
    void testHandleExpenseDeleted_WithEmptyUserShares_HandlesGracefully() {
        // Arrange
        Long expenseId = 8L;
        Long groupId = 8L;
        Long paidByUserId = 8L;
        BigDecimal totalAmount = new BigDecimal("40.00");
        String splitType = "UNEQUAL";
        List<UserShareDto> emptyUserShares = Arrays.asList();
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseDeleted(expenseId, groupId, paidByUserId,
                totalAmount, splitType, null, emptyUserShares);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getUserShares() != null &&
            request.getUserShares().isEmpty() &&
            request.getTotalAmount().equals(totalAmount.negate())
        ));
    }

    @Test
    void testHandleExpenseCreated_WithZeroAmount_Success() {
        // Arrange
        Long expenseId = 9L;
        Long groupId = 9L;
        Long paidByUserId = 9L;
        BigDecimal totalAmount = BigDecimal.ZERO;
        String splitType = "EQUAL";
        String description = "Zero amount expense";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseCreated(expenseId, groupId, paidByUserId,
                totalAmount, splitType, groupUserIds, null, description);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getTotalAmount().equals(BigDecimal.ZERO)
        ));
    }

    @Test
    void testHandleExpenseDeleted_WithNegativeAmount_Success() {
        // Arrange
        Long expenseId = 10L;
        Long groupId = 10L;
        Long paidByUserId = 10L;
        BigDecimal totalAmount = new BigDecimal("-25.00");
        String splitType = "EQUAL";
       
        doNothing().when(balanceService).updateBalancesForExpense(any(ExpenseBalanceUpdateRequest.class));

        // Act
        expenseIntegrationService.handleExpenseDeleted(expenseId, groupId, paidByUserId,
                totalAmount, splitType, groupUserIds, null);

        // Assert
        verify(balanceService).updateBalancesForExpense(argThat(request ->
            request.getTotalAmount().equals(new BigDecimal("25.00")) // Negated negative becomes positive
        ));
    }
}