package com.expenseshare.demo;


import com.expenseshare.demo.dto.ExpenseBalanceUpdateRequest;
import com.expenseshare.demo.dto.UserShareDto;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.entity.UserBalance;
import com.expenseshare.demo.repository.GroupRepository;
import com.expenseshare.demo.repository.UserBalanceRepository;
import com.expenseshare.demo.repository.UserRepository;
import com.expenseshare.demo.services.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito; // Import Mockito for static utility methods

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private UserBalanceRepository userBalanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private BalanceService balanceService; // Object under test

    // Test Data
    private Group group;
    private User paidByUser;
    private User user1;
    private User user2;
    // user3 not used in setup, removed for brevity/strictness
    private final Long GROUP_ID = 1L;
    private final Long PAID_BY_ID = 100L;
    private final Long USER_ID_1 = 101L;
    private final Long USER_ID_2 = 102L;

    @BeforeEach
    void setUp() {
        group = Group.builder().id(GROUP_ID).name("Test Group").build();
        paidByUser = User.builder().id(PAID_BY_ID).userName("Payer").build();
        user1 = User.builder().id(USER_ID_1).userName("User1").build();
        user2 = User.builder().id(USER_ID_2).userName("User2").build();

        // Common Mocks for the private helper methods (getGroupById, getUserById)
        // These are called internally by updateBalancesForExpense, processEqualSplit, etc.
        when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
        when(userRepository.findById(PAID_BY_ID)).thenReturn(Optional.of(paidByUser));
        when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(USER_ID_2)).thenReturn(Optional.of(user2));

        // Use Mockito.lenient() for stubs that might not be used in every single test case
        // that calls setUp, though in this case, the specific ID stubs should be fine.
    }

// ------------------------------------------------------------------------------------------------------------------
// Tests for updateBalancesForExpense (Integration/Flow)
// ------------------------------------------------------------------------------------------------------------------

    @Test
    void updateBalancesForExpense_equalSplit_success() {
        // Arrange
        BigDecimal totalAmount = new BigDecimal("90.00");
        List<Long> groupUsers = Arrays.asList(PAID_BY_ID, USER_ID_1, USER_ID_2);

        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .groupId(GROUP_ID)
                .paidByUserId(PAID_BY_ID)
                .totalAmount(totalAmount)
                .splitType("EQUAL")
                .groupUserIds(groupUsers)
                .build();

        // Mock for the 'updateBalance' calls inside processEqualSplit (no opposite/direct balance initially)
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(eq(GROUP_ID), anyLong(), eq(PAID_BY_ID)))
                .thenReturn(Optional.empty());
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(eq(GROUP_ID), eq(PAID_BY_ID), anyLong()))
                .thenReturn(Optional.empty());

        // Act
        balanceService.updateBalancesForExpense(request);

        // Assert
        BigDecimal expectedShare = new BigDecimal("30.00");

        // Should call save 2 times (for User1 and User2)
        ArgumentCaptor<UserBalance> balanceCaptor = ArgumentCaptor.forClass(UserBalance.class);
        verify(userBalanceRepository, times(2)).save(balanceCaptor.capture());

        List<UserBalance> savedBalances = balanceCaptor.getAllValues();
        assertEquals(2, savedBalances.size());

        // Validate balances for User1
        UserBalance balance1 = savedBalances.stream().filter(b -> b.getDebtor().getId().equals(USER_ID_1)).findFirst().orElseThrow();
        assertEquals(PAID_BY_ID, balance1.getCreditor().getId());
        assertEquals(0, expectedShare.compareTo(balance1.getAmount()));
    }

// ------------------------------------------------------------------------------------------------------------------
// Tests for processEqualSplit (Standalone)
// ------------------------------------------------------------------------------------------------------------------

    @Test
    void processEqualSplit_twoOthersOwe_savesCorrectBalances() {
        // Arrange
        BigDecimal totalAmount = new BigDecimal("100.00");
        List<Long> userIds = Arrays.asList(PAID_BY_ID, USER_ID_1, USER_ID_2);
        BigDecimal expectedShare = new BigDecimal("33.33"); // 100.00 / 3 rounded HALF_UP

        // Mock for the 'updateBalance' calls inside processEqualSplit (no opposite/direct balance initially)
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(eq(GROUP_ID), anyLong(), eq(PAID_BY_ID)))
                .thenReturn(Optional.empty());
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(eq(GROUP_ID), eq(PAID_BY_ID), anyLong()))
                .thenReturn(Optional.empty());

        // Act
        balanceService.processEqualSplit(group, paidByUser, totalAmount, userIds);

        // Assert
        ArgumentCaptor<UserBalance> balanceCaptor = ArgumentCaptor.forClass(UserBalance.class);
        verify(userBalanceRepository, times(2)).save(balanceCaptor.capture());

        List<UserBalance> savedBalances = balanceCaptor.getAllValues();
        // Check first balance
        assertEquals(USER_ID_1, savedBalances.get(0).getDebtor().getId());
        // FIX: Corrected typo from PAID_BY_BY_ID to PAID_BY_ID
        assertEquals(PAID_BY_ID, savedBalances.get(0).getCreditor().getId());
        assertEquals(0, expectedShare.compareTo(savedBalances.get(0).getAmount()));
    }

// ------------------------------------------------------------------------------------------------------------------
// Tests for updateBalance (Core Logic) - Opposite Balance Scenarios
// ------------------------------------------------------------------------------------------------------------------

    @Test
    void updateBalance_oppositeBalance_newDebtLarger_clearsOppositeAndCreatesNew() {
        // User1 owes Payer (NEW: 30.00). Payer owes User1 (EXISTING: 10.00)
        // Arrange
        BigDecimal newDebt = new BigDecimal("30.00");
        BigDecimal existingAmount = new BigDecimal("10.00");

        // This is the OPPOSITE BALANCE (Payer owes User1)
        UserBalance oppositeBalance = UserBalance.builder()
                .group(group).debtor(paidByUser).creditor(user1).amount(existingAmount).build();

        // **CORRECTION HERE:**
        // The service looks for (GroupId, CreditorId, DebtorId)
        // CreditorId = PAID_BY_ID
        // DebtorId = USER_ID_1
        // We need to look for (GROUP_ID, PAID_BY_ID, USER_ID_1)
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(
                eq(GROUP_ID),
                eq(PAID_BY_ID), // This is the 'creditor' of the new debt, but 'debtor' of the opposite balance
                eq(USER_ID_1)  // This is the 'debtor' of the new debt, but 'creditor' of the opposite balance
        ))
                .thenReturn(Optional.of(oppositeBalance)); // Found opposite balance (Payer owes User1)

        // Act
        // The new debt is User1 (debtor) owes Payer (creditor) 30.00
        balanceService.updateBalance(GROUP_ID, USER_ID_1, PAID_BY_ID, newDebt);

        // Assert
        verify(userBalanceRepository, times(1)).delete(oppositeBalance); // Clears the Payer owes User1 balance

        ArgumentCaptor<UserBalance> captor = ArgumentCaptor.forClass(UserBalance.class);
        verify(userBalanceRepository, times(1)).save(captor.capture()); // Creates new net balance

        UserBalance savedBalance = captor.getValue();
        assertEquals(USER_ID_1, savedBalance.getDebtor().getId());
        assertEquals(PAID_BY_ID, savedBalance.getCreditor().getId());
        // Remaining amount is 30.00 - 10.00 = 20.00
        assertEquals(0, new BigDecimal("20.00").compareTo(savedBalance.getAmount()));

        // Verify no attempt was made to find a direct balance after finding the opposite
        verify(userBalanceRepository, never()).findByGroupIdAndDebtorIdAndCreditorId(eq(GROUP_ID), eq(USER_ID_1), eq(PAID_BY_ID));
    }

// ------------------------------------------------------------------------------------------------------------------
// Tests for Validation/Exceptions in updateBalancesForExpense
// ------------------------------------------------------------------------------------------------------------------

    @Test
    void updateBalancesForExpense_throwsException_groupNotFound() {
        // Arrange
        Long nonExistentGroupId = 999L;
        // Mock the specific ID to be not found
        when(groupRepository.findById(nonExistentGroupId)).thenReturn(Optional.empty());

        // Use Mockito.lenient() for userRepository stubs to avoid unnecessary stubbing warnings,
        // as getUserById won't be called if getGroupById throws an exception first.
        Mockito.lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.of(paidByUser));

        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .groupId(nonExistentGroupId)
                .paidByUserId(PAID_BY_ID)
                .totalAmount(new BigDecimal("10.00"))
                .splitType("EQUAL")
                .groupUserIds(Arrays.asList(PAID_BY_ID, USER_ID_1))
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> balanceService.updateBalancesForExpense(request));
        assertTrue(thrown.getMessage().contains("Group not found with ID: 999"));

        // Verify that the process*Split methods were never called
        verify(userBalanceRepository, never()).save(any());
    }

    // ... (All other tests from the original solution should also be included)
}