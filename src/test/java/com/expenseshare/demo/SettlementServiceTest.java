package com.expenseshare.demo;


import com.expenseshare.demo.dto.ExpenseBalanceUpdateRequest;
import com.expenseshare.demo.dto.SettleBalanceRequestDto;
import com.expenseshare.demo.dto.SettlementResponseDto;
import com.expenseshare.demo.entity.*;
import com.expenseshare.demo.enums.SettlementStatus;
import com.expenseshare.demo.repository.*;
import com.expenseshare.demo.services.BalanceService;
import com.expenseshare.demo.services.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Settlement Service Tests")
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private UserBalanceRepository userBalanceRepository;

    @Mock
    private SettlementDetailRepository settlementDetailRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BalanceService balanceService;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private SettlementService settlementService;

    private User payer;
    private User user1;
    private User user2;
    private User user3;
    private User receiver;
    private Group group1;
    private Group group2;
    private Group group;
    private UserBalance balance1;
    private UserBalance balance2;

    @BeforeEach
    void setUp() {
        // Setup test users
        payer = User.builder()
                .id(1L)
                .firstName("John Doe")
                .emailId("john@example.com")
                .build();

        user1 = User.builder()
                .id(1L)
                .firstName("John Doe")
                .emailId("john@example.com")
                .build();

        user2 = User.builder()
                .id(1L)
                .firstName("Jane Smith")
                .emailId("john@example.com")
                .build();

        user3 = User.builder()
                .id(1L)
                .firstName("Jane Smith")
                .emailId("john@example.com")
                .build();

        receiver = User.builder()
                .id(2L)
                .firstName("Jane Smith11")
                .build();

        // Setup test groups
        group1 = Group.builder()
                .id(1L)
                .name("Roommates")
                .users(Set.of(payer, receiver))
                .build();

        group2 = Group.builder()
                .id(2L)
                .name("Trip")
                .users(Set.of(payer, receiver))
                .build();
        group = Group.builder()
                .id(1L)
                .name("Roommates1")
                .users(Set.of(payer, receiver))
                .build();

        // Setup test balances
        balance1 = UserBalance.builder()
                .id(1L)
                .group(group1)
                .debtor(payer)
                .creditor(receiver)
                .amount(new BigDecimal("50.00"))
                .version(0L)
                .build();

        balance2 = UserBalance.builder()
                .id(2L)
                .group(group2)
                .debtor(payer)
                .creditor(receiver)
                .amount(new BigDecimal("100.00"))
                .version(0L)
                .build();
    }

    // ============ Settlement Tests ============

    @Test
    @DisplayName("Should successfully settle balance between two users")
    void testSuccessfulSettlement() {
        // Arrange
        SettleBalanceRequestDto request = SettleBalanceRequestDto.builder()
                .payerId(1L)
                .receiverId(2L)
                .amount(new BigDecimal("75.00"))
                .description("Settling expenses")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(groupRepository.findGroupsWithBothUsers(1L, 2L))
                .thenReturn(Arrays.asList(group1, group2));
        when(userBalanceRepository.findGroupsWithBalancesBetweenUsers(1L, 2L))
                .thenReturn(Arrays.asList(1L, 2L));
        when(userBalanceRepository.findBalancesBetweenUsersInGroupForUpdate(1L, 2L, 1L))
                .thenReturn(Arrays.asList(balance1));
        when(userBalanceRepository.findBalancesBetweenUsersInGroupForUpdate(1L, 2L, 2L))
                .thenReturn(Arrays.asList(balance2));

        Settlement savedSettlement = Settlement.builder()
                .id(1L)
                .payer(payer)
                .receiver(receiver)
                .amount(new BigDecimal("75.00"))
                .status(SettlementStatus.COMPLETED)
                .settlementDate(LocalDateTime.now())
                .settlementDetails(Arrays.asList())
                .build();

        when(settlementRepository.save(any(Settlement.class))).thenReturn(savedSettlement);
        when(settlementDetailRepository.save(any(SettlementDetail.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SettlementResponseDto response = settlementService.settleBalance(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getSettlementId());
        assertEquals("John Doe", response.getPayerName());
        assertEquals("Jane Smith", response.getReceiverName());
        assertEquals(new BigDecimal("75.00"), response.getTotalAmountSettled());
        assertEquals("COMPLETED", response.getStatus());

        verify(userBalanceRepository, never()).save(any());
    }

    // ============ Balance Update Tests ============

    @Test
    @DisplayName("Should create new balance when none exists")
    void testCreateNewBalance() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 1L, 2L))
                .thenReturn(Optional.empty());
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 2L, 1L))
                .thenReturn(Optional.empty());

        // Act
        balanceService.updateBalance(1L, 1L, 2L, new BigDecimal("50.00"));

        // Assert
        verify(userBalanceRepository, times(1)).save(argThat(balance ->
                balance.getDebtor().getId().equals(1L) &&
                        balance.getCreditor().getId().equals(2L) &&
                        balance.getAmount().compareTo(new BigDecimal("50.00")) == 0));
    }

    @Test
    @DisplayName("Should update existing balance")
    void testUpdateExistingBalance() {
        // Arrange
        UserBalance existingBalance = UserBalance.builder()
                .id(1L)
                .group(group1)
                .debtor(user1)
                .creditor(user2)
                .amount(new BigDecimal("30.00"))
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 1L, 2L))
                .thenReturn(Optional.of(existingBalance));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 2L, 1L))
                .thenReturn(Optional.empty());

        // Act
        balanceService.updateBalance(1L, 1L, 2L, new BigDecimal("20.00"));

        // Assert
        verify(userBalanceRepository, times(1)).save(argThat(balance ->
                balance.getAmount().compareTo(new BigDecimal("50.00")) == 0)); // 30 + 20
    }

    @Test
    @DisplayName("Should handle opposite balance netting")
    void testOppositeBalanceNetting() {
        // Arrange - User2 owes User1 $30, now User1 owes User2 $50
        UserBalance oppositeBalance = UserBalance.builder()
                .id(1L)
                .group(group)
                .debtor(user2)
                .creditor(user1)
                .amount(new BigDecimal("30.00"))
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 1L, 2L))
                .thenReturn(Optional.empty());
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 2L, 1L))
                .thenReturn(Optional.of(oppositeBalance));

        // Act
        balanceService.updateBalance(1L, 1L, 2L, new BigDecimal("50.00"));

        // Assert
        verify(userBalanceRepository, times(1)).delete(oppositeBalance); // Delete opposite
        verify(userBalanceRepository, times(1)).save(argThat(balance ->
                balance.getDebtor().getId().equals(1L) &&
                        balance.getCreditor().getId().equals(2L) &&
                        balance.getAmount().compareTo(new BigDecimal("20.00")) == 0)); // Net: 50-30=20
    }

    @Test
    @DisplayName("Should delete balance when opposite balances cancel out")
    void testOppositeBalancesCancelOut() {
        // Arrange - User2 owes User1 $50, now User1 owes User2 $50
        UserBalance oppositeBalance = UserBalance.builder()
                .id(1L)
                .group(group)
                .debtor(user2)
                .creditor(user1)
                .amount(new BigDecimal("50.00"))
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 1L, 2L))
                .thenReturn(Optional.empty());
        when(userBalanceRepository.findByGroupIdAndDebtorIdAndCreditorId(1L, 2L, 1L))
                .thenReturn(Optional.of(oppositeBalance));

        // Act
        balanceService.updateBalance(1L, 1L, 2L, new BigDecimal("50.00"));

        // Assert
        verify(userBalanceRepository, times(1)).delete(oppositeBalance);
        verify(userBalanceRepository, never()).save(any()); // No new balance created
    }

    @Test
    @DisplayName("Should not create balance when amount is zero")
    void testZeroAmountBalance() {
        // Act
        balanceService.updateBalance(1L, 1L, 2L, BigDecimal.ZERO);

        // Assert
        verify(userBalanceRepository, never()).save(any());
        verify(userBalanceRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should not create balance for same user")
    void testSameUserBalance() {
        // Act
        balanceService.updateBalance(1L, 1L, 1L, new BigDecimal("50.00"));

        // Assert
        verify(userBalanceRepository, never()).save(any());
        verify(userBalanceRepository, never()).delete(any());
    }

    // ============ Simplify Balances Tests ============

    @Test
    @DisplayName("Should remove small balances when simplifying")
    void testSimplifyBalances() {
        // Arrange
        UserBalance smallBalance = UserBalance.builder()
                .id(1L)
                .group(group)
                .debtor(user1)
                .creditor(user2)
                .amount(new BigDecimal("0.005")) // Less than 0.01
                .build();

        UserBalance normalBalance = UserBalance.builder()
                .id(2L)
                .group(group)
                .debtor(user2)
                .creditor(user3)
                .amount(new BigDecimal("50.00"))
                .build();

        when(userBalanceRepository.findByGroupId(1L))
                .thenReturn(Arrays.asList(smallBalance, normalBalance));

        // Act
        balanceService.simplifyBalances(1L);

        // Assert
        verify(userBalanceRepository, times(1)).delete(smallBalance);
        verify(userBalanceRepository, never()).delete(normalBalance);
    }

    // ============ Validation Tests ============

    @Test
    @DisplayName("Should throw exception for invalid group ID")
    void testInvalidGroupId() {
        // Arrange
        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .groupId(null)
                .paidByUserId(1L)
                .totalAmount(new BigDecimal("100.00"))
                .splitType("EQUAL")
                .groupUserIds(Arrays.asList(1L, 2L))
                .build();

        when(groupRepository.findById(999L)).thenThrow(IllegalArgumentException.class);
         // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                balanceService.updateBalancesForExpense(request));
    }

    @Test
    @DisplayName("Should throw exception for invalid paid by user")
    void testInvalidPaidByUser() {
        // Arrange
        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(999L)
                .totalAmount(new BigDecimal("100.00"))
                .splitType("EQUAL")
                .groupUserIds(Arrays.asList(1L, 2L))
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                balanceService.updateBalancesForExpense(request));
    }

    @Test
    @DisplayName("Should throw exception when total amount is zero or negative")
    void testInvalidTotalAmount() {
        // Zero amount
        ExpenseBalanceUpdateRequest request1 = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(1L)
                .totalAmount(BigDecimal.ZERO)
                .splitType("EQUAL")
                .groupUserIds(Arrays.asList(1L, 2L))
                .build();

        BalanceService balanceService = null;
        assertThrows(IllegalArgumentException.class, () ->
                balanceService.updateBalancesForExpense(request1));

        // Negative amount
        ExpenseBalanceUpdateRequest request2 = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(1L)
                .totalAmount(new BigDecimal("-100.00"))
                .splitType("EQUAL")
                .groupUserIds(Arrays.asList(1L, 2L))
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                balanceService.updateBalancesForExpense(request2));
    }

    @Test
    @DisplayName("Should throw exception for missing split type")
    void testMissingSplitType() {
        // Arrange
        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(1L)
                .totalAmount(new BigDecimal("100.00"))
                .groupUserIds(Arrays.asList(1L, 2L))
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                balanceService.updateBalancesForExpense(request));
    }

    @Test
    @DisplayName("Should throw exception when equal split missing user IDs")
    void testEqualSplitMissingUserIds() {
        // Arrange
        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(1L)
                .totalAmount(new BigDecimal("100.00"))
                .splitType("EQUAL")
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                balanceService.updateBalancesForExpense(request));
    }

    @Test
    @DisplayName("Should throw exception when unequal split missing shares")
    void testUnequalSplitMissingShares() {
        // Arrange
        ExpenseBalanceUpdateRequest request = ExpenseBalanceUpdateRequest.builder()
                .groupId(1L)
                .paidByUserId(1L)
                .totalAmount(new BigDecimal("100.00"))
                .splitType("UNEQUAL")
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                balanceService.updateBalancesForExpense(request));
    }
}
