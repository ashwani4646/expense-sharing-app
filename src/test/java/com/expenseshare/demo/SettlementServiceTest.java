package com.expenseshare.demo;

import com.expenseshare.demo.dto.SettleBalanceRequestDto;
import com.expenseshare.demo.dto.SettlementResponseDto;
import com.expenseshare.demo.dto.UserBalanceResponseDto;
import com.expenseshare.demo.entity.*;
import com.expenseshare.demo.enums.SettlementStatus;
import com.expenseshare.demo.exception.InvalidSettlementException;
import com.expenseshare.demo.exception.UserNotFoundException;
import com.expenseshare.demo.repository.*;
import com.expenseshare.demo.services.SettlementService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private GroupRepository groupRepository;
   
    @InjectMocks
    private SettlementService settlementService;
   
    private SettleBalanceRequestDto validRequest;
    private User payer;
    private User receiver;
    private Group testGroup;
    private UserBalance userBalance;
    private Settlement testSettlement;

    @BeforeEach
    void setUp() {
        payer = User.builder()
                .id(1L)
                .userName("payer")
                .build();
       
        receiver = User.builder()
                .id(2L)
                .userName("receiver")
                .build();

        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .build();

        userBalance = UserBalance.builder()
                .id(1L)
                .group(testGroup)
                .debtor(payer)
                .creditor(receiver)
                .amount(new BigDecimal("50.00"))
                .build();

        testSettlement = Settlement.builder()
                .id(1L)
                .payer(payer)
                .receiver(receiver)
                .amount(new BigDecimal("30.00"))
                .status(SettlementStatus.COMPLETED)
                .build();

        validRequest = SettleBalanceRequestDto.builder()
                .payerId(1L)
                .receiverId(2L)
                .amount(new BigDecimal("30.00"))
                .description("Test settlement")
                .build();
    }

   


    @Test
    void testSettleBalance_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(groupRepository.findGroupsWithBothUsers(1L, 2L)).thenReturn(Arrays.asList(testGroup));
        when(userBalanceRepository.findGroupsWithBalancesBetweenUsers(1L, 2L)).thenReturn(Arrays.asList(1L));
        when(userBalanceRepository.findBalancesBetweenUsersInGroupForUpdate(1L, 2L, 1L))
                .thenReturn(Arrays.asList(userBalance));
        SettlementDetail settlementDetail = new SettlementDetail();
        Group group= new Group();
        group.setId(1L);
        settlementDetail.setId(1L);
        settlementDetail.setGroup(group);
        testSettlement.setSettlementDetails(List.of(settlementDetail));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(testSettlement);
        when(settlementDetailRepository.save(any(SettlementDetail.class)))
                .thenReturn(new SettlementDetail());

        // Act
        SettlementResponseDto result = settlementService.settleBalance(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getSettlementId());
        verify(settlementRepository).save(any(Settlement.class));
        verify(userBalanceRepository).save(userBalance);
    }

    @Test
    void testSettleBalance_InvalidPayerId_ThrowsException() {
        // Arrange
        validRequest.setPayerId(null);

        // Act & Assert
        assertThrows(InvalidSettlementException.class, () ->
            settlementService.settleBalance(validRequest));
    }

    @Test
    void testSettleBalance_SamePayerAndReceiver_ThrowsException() {
        // Arrange
        validRequest.setReceiverId(1L);

        // Act & Assert
        assertThrows(InvalidSettlementException.class, () ->
            settlementService.settleBalance(validRequest));
    }

    @Test
    void testSettleBalance_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
            settlementService.settleBalance(validRequest));
    }

    @Test
    void testGetUserBalance_Success() {
        // Arrange
        List<UserBalance> balances = Arrays.asList(userBalance);
        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userBalanceRepository.findAllBalancesForUser(1L)).thenReturn(balances);

        // Act
        UserBalanceResponseDto result = settlementService.getUserBalance(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        verify(userBalanceRepository).findAllBalancesForUser(1L);
    }

    @Test
    void testGetUserSettlements_Success() {
        // Arrange
        List<Settlement> settlements = Arrays.asList(testSettlement);
        testSettlement.setSettlementDetails(Lists.newArrayList());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(settlementRepository.findSettlementsByUser(1L)).thenReturn(settlements);

        // Act
        List<SettlementResponseDto> result = settlementService.getUserSettlements(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(settlementRepository).findSettlementsByUser(1L);
    }

    @Test
    void testFindSettlementsBetweenUsers_Success() {
        // Arrange
        List<Settlement> settlements = Arrays.asList(testSettlement);
        when(settlementRepository.findSettlementsBetweenUsers(1L, 2L)).thenReturn(settlements);

        // Act
        List<Settlement> result = settlementService.findSettlementsBetweenUsers(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(settlementRepository).findSettlementsBetweenUsers(1L, 2L);
    }
}
