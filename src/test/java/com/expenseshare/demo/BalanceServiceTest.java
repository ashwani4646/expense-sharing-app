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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private UserBalanceRepository userBalanceRepository;
   
    @Mock
    private UserRepository userRepository;
   
    @Mock
    private GroupRepository groupRepository;
   
    @InjectMocks
    private BalanceService balanceService;
   
    private Group testGroup;
    private User paidByUser;
    private User user1;
    private User user2;
    private ExpenseBalanceUpdateRequest equalSplitRequest;
    private ExpenseBalanceUpdateRequest unequalSplitRequest;

    @BeforeEach
    void setUp() {
        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .build();
       
        paidByUser = User.builder()
                .id(1L)
                .userName("payer")
                .build();
       
        user1 = User.builder()
                .id(2L)
                .userName("user1")
                .build();
       
        user2 = User.builder()
                .id(3L)
                .userName("user2")
                .build();

        equalSplitRequest = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(1L)
                .totalAmount(new BigDecimal("30.00"))
                .splitType("EQUAL")
                .groupUserIds(Arrays.asList(1L, 2L, 3L))
                .build();

        List<UserShareDto> userShares = Arrays.asList(
                UserShareDto.builder().userId(1L).share(new BigDecimal("10.00")).build(),
                UserShareDto.builder().userId(2L).share(new BigDecimal("15.00")).build(),
                UserShareDto.builder().userId(3L).share(new BigDecimal("5.00")).build()
        );

        unequalSplitRequest = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(1L)
                .totalAmount(new BigDecimal("30.00"))
                .splitType("UNEQUAL")
                .userShares(userShares)
                .build();
    }

    @Test
    void testUpdateBalancesForExpense_EqualSplit_Success() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(1L)).thenReturn(Optional.of(paidByUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user2));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act
        balanceService.updateBalancesForExpense(equalSplitRequest);

        // Assert
        verify(userRepository, times(7)).findById(anyLong());
    }

    @Test
    void testUpdateBalancesForExpense_UnequalSplit_Success() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(1L)).thenReturn(Optional.of(paidByUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user2));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(anyLong(), anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // Act
        balanceService.updateBalancesForExpense(unequalSplitRequest);

        // Assert
        verify(userRepository, times(7)).findById(anyLong());
        verify(userBalanceRepository, times(2)).save(any(UserBalance.class));
    }

    @Test
    void testUpdateBalancesForExpense_InvalidGroupId_ThrowsException() {
        // Arrange
        equalSplitRequest.setGroupId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            balanceService.updateBalancesForExpense(equalSplitRequest));
    }

    @Test
    void testUpdateBalancesForExpense_GroupNotFound_ThrowsException() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            balanceService.updateBalancesForExpense(equalSplitRequest));
    }

    @Test
    void testUpdateBalancesForExpense_UserNotFound_ThrowsException() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            balanceService.updateBalancesForExpense(equalSplitRequest));
    }

    @Test
    void testUpdateBalance_ExistingBalance_UpdatesSuccessfully() {
        // Arrange
        UserBalance existingBalance = UserBalance.builder()
                .group(testGroup)
                .debtor(user1)
                .creditor(paidByUser)
                .amount(new BigDecimal("5.00"))
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(paidByUser));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 1L, 2L))
                .thenReturn(Optional.empty());
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 2L, 1L))
                .thenReturn(Optional.of(existingBalance));

        // Act
        balanceService.updateBalance(1L, 2L, 1L, new BigDecimal("10.00"));

        // Assert
        verify(userBalanceRepository).save(existingBalance);
        assertEquals(new BigDecimal("15.00"), existingBalance.getAmount());
    }

    @Test
    void testSimplifyBalances_RemovesSmallBalances() {
        // Arrange
        List<UserBalance> smallBalances = Arrays.asList(
                UserBalance.builder()
                        .debtor(user1)
                        .creditor(paidByUser)
                        .amount(new BigDecimal("0.005"))
                        .build()
        );

        when(userBalanceRepository.findByGroupId(1L)).thenReturn(smallBalances);

        // Act
        balanceService.simplifyBalances(1L);

        // Assert
        verify(userBalanceRepository).delete(any(UserBalance.class));
    }

    @Test
    void testValidateExpenseBalanceRequest_InvalidSplitType_ThrowsException() {
        // Arrange
        equalSplitRequest.setSplitType("INVALID");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            balanceService.updateBalancesForExpense(equalSplitRequest));
    }
}