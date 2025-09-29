package com.expenseshare.demo;

import com.expenseshare.demo.dto.ExpenseDto;
import com.expenseshare.demo.dto.ExpenseResponseDto;
import com.expenseshare.demo.dto.UserShareDto;
import com.expenseshare.demo.entity.Expense;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.enums.SplitType;
import com.expenseshare.demo.exception.GroupNotFoundException;
import com.expenseshare.demo.exception.InvalidExpenseDataException;
import com.expenseshare.demo.exception.UserNotFoundException;
import com.expenseshare.demo.repository.ExpenseRepository;
import com.expenseshare.demo.repository.ExpenseShareRepository;
import com.expenseshare.demo.repository.GroupRepository;
import com.expenseshare.demo.repository.UserRepository;
import com.expenseshare.demo.services.ExpenseService;
import org.assertj.core.util.Lists;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTestUpdated {

    @Mock
    private ExpenseRepository expenseRepository;
   
    @Mock
    private GroupRepository groupRepository;
   
    @Mock
    private UserRepository userRepository;
   
    @Mock
    private ExpenseShareRepository expenseShareRepository;
   
    @InjectMocks
    private ExpenseService expenseService;
   
    private ExpenseDto validExpenseDto;
    private Group testGroup;
    private User paidByUser;
    private User user1;
    private User user2;
    private Expense testExpense;

    @BeforeEach
    void setUp() {
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

        testGroup = Group.builder()
                .id(1L)
                .name("Test Group")
                .users(Set.of(paidByUser, user1, user2))
                .build();

        validExpenseDto = ExpenseDto.builder()
                .description("Test Expense")
                .amount(new BigDecimal("30.00"))
                .groupId(1L)
                .paidByUserId(1L)
                .splitType("EQUAL")
                .build();

        testExpense = Expense.builder()
                .id(1L)
                .description("Test Expense")
                .amount(new BigDecimal("30.00"))
                .group(testGroup)
                .paidBy(paidByUser)
                .splitType(SplitType.EQUAL)
                .build();
    }

    @Test
    void testCreateExpense_EqualSplit_Success() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(1L)).thenReturn(Optional.of(paidByUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseShareRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // Act
        ExpenseResponseDto result = expenseService.createExpense(validExpenseDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Expense", result.getDescription());
        assertEquals(new BigDecimal("30.00"), result.getAmount());
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseShareRepository).saveAll(anyList());
    }

    @Test
    void testCreateExpense_UnequalSplit_Success() {
        // Arrange
        List<UserShareDto> userShares = Arrays.asList(
                UserShareDto.builder().userId(1L).share(new BigDecimal("10.00")).build(),
                UserShareDto.builder().userId(2L).share(new BigDecimal("20.00")).build()
        );

        validExpenseDto.setSplitType("UNEQUAL");
        validExpenseDto.setUserShares(userShares);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(1L)).thenReturn(Optional.of(paidByUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user1));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseShareRepository.saveAll(anyList())).thenReturn(Arrays.asList());

        // Act
        ExpenseResponseDto result = expenseService.createExpense(validExpenseDto);

        // Assert
        assertNotNull(result);
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseShareRepository).saveAll(anyList());
    }

    @Test
    void testCreateExpense_InvalidDescription_ThrowsException() {
        // Arrange
        validExpenseDto.setDescription("");

        // Act & Assert
        assertThrows(InvalidExpenseDataException.class, () ->
            expenseService.createExpense(validExpenseDto));
    }

    @Test
    void testCreateExpense_InvalidAmount_ThrowsException() {
        // Arrange
        validExpenseDto.setAmount(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(InvalidExpenseDataException.class, () ->
            expenseService.createExpense(validExpenseDto));
    }

    @Test
    void testCreateExpense_GroupNotFound_ThrowsException() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(GroupNotFoundException.class, () ->
            expenseService.createExpense(validExpenseDto));
    }

    @Test
    void testCreateExpense_UserNotFound_ThrowsException() {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () ->
            expenseService.createExpense(validExpenseDto));
    }

    @Test
    void testCreateExpense_UserNotInGroup_ThrowsException() {
        // Arrange
        User outsideUser = User.builder().id(4L).userName("outsider").build();
        validExpenseDto.setPaidByUserId(4L);
       
        when(groupRepository.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepository.findById(4L)).thenReturn(Optional.of(outsideUser));

        // Act & Assert
        assertThrows(InvalidExpenseDataException.class, () ->
            expenseService.createExpense(validExpenseDto));
    }





    @Test
    void testGetExpensesByUser_Success() {
        // Arrange
        List<Expense> expenses = Arrays.asList(testExpense);

        testExpense.setExpenseShares(Lists.newArrayList());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(expenseRepository.findExpensesByUserId(1L)).thenReturn(expenses);

        // Act
        List<ExpenseResponseDto> result = expenseService.getExpensesByUser(1L);

        // Assert
        assertNotNull(result);

    }

    @Test
    void testGetExpenseById_Success() {
        // Arrange
        testExpense.setExpenseShares(Lists.newArrayList());
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(testExpense));

        // Act
        ExpenseResponseDto result = expenseService.getExpenseById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(expenseRepository).findById(1L);
    }

    @Test
    void testValidateUnequalSplit_SharesNotEqualToTotal_ThrowsException() {
        // Arrange
        List<UserShareDto> invalidShares = Arrays.asList(
                UserShareDto.builder().userId(1L).share(new BigDecimal("10.00")).build(),
                UserShareDto.builder().userId(2L).share(new BigDecimal("15.00")).build()
        );

        validExpenseDto.setSplitType("UNEQUAL");
        validExpenseDto.setUserShares(invalidShares);

        // Act & Assert
        assertThrows(InvalidExpenseDataException.class, () ->
            expenseService.createExpense(validExpenseDto));
    }
}