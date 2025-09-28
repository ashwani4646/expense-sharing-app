package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.ExpenseDto;
import com.expenseshare.demo.dto.ExpenseResponseDto;
import com.expenseshare.demo.dto.ExpenseShareResponseDto;
import com.expenseshare.demo.entity.Expense;
import com.expenseshare.demo.entity.ExpenseShare;
import com.expenseshare.demo.entity.Group;
import com.expenseshare.demo.entity.User;
import com.expenseshare.demo.enums.SplitType;
import com.expenseshare.demo.exception.GroupNotFoundException;
import com.expenseshare.demo.exception.InvalidExpenseDataException;
import com.expenseshare.demo.dto.UserShareDto;
import com.expenseshare.demo.exception.UserNotFoundException;
import com.expenseshare.demo.repository.ExpenseRepository;
import com.expenseshare.demo.repository.ExpenseShareRepository;
import com.expenseshare.demo.repository.GroupRepository;
import com.expenseshare.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ExpenseShareRepository expenseShareRepository;

    public ExpenseService(ExpenseRepository expenseRepository, GroupRepository groupRepository, UserRepository userRepository, ExpenseShareRepository expenseShareRepository){
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.expenseShareRepository = expenseShareRepository;
    }
    @Transactional
    public ExpenseResponseDto createExpense(ExpenseDto expenseDto) {
        log.info("Creating expense: {}", expenseDto.getDescription());

        // Validate input
        validateCreateExpenseexpenseDto(expenseDto);

        // Fetch group and validate
        Group group = groupRepository.findById(expenseDto.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found with id: " + expenseDto.getGroupId()));

        // Fetch paid by user and validate
        User paidByUser = userRepository.findById(expenseDto.getPaidByUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + expenseDto.getPaidByUserId()));

        // Validate that paid by user is part of the group
        if (!group.getUsers().contains(paidByUser)) {
            throw new InvalidExpenseDataException("User is not part of the group");
        }

        // Create expense
        Expense expense = Expense.builder()
                .description(expenseDto.getDescription())
                .amount(expenseDto.getAmount())
                .group(group)
                .paidBy(paidByUser)
                .splitType(SplitType.valueOf(expenseDto.getSplitType().toUpperCase()))
                .build();

        expense = expenseRepository.save(expense);

        // Create expense shares
        List<ExpenseShare> expenseShares = createExpenseShares(expense, expenseDto, group);
        expenseShareRepository.saveAll(expenseShares);
        expense.setExpenseShares(expenseShares);

        log.info("Expense created successfully with id: {}", expense.getId());
        return mapToExpenseResponse(expense);
    }

    public List<ExpenseResponseDto> getExpensesByGroup(Long groupId) {
        log.info("Fetching expenses for group id: {}", groupId);

        if (!groupRepository.existsById(groupId)) {
            throw new GroupNotFoundException("Group not found with id: " + groupId);
        }

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        return expenses.stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponseDto> getExpensesByUser(Long userId) {
        log.info("Fetching expenses for user id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        List<Expense> expenses = expenseRepository.findExpensesByUserId(userId);
        return expenses.stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponseDto getExpenseById(Long expenseId) {
        log.info("Fetching expense with id: {}", expenseId);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));

        return mapToExpenseResponse(expense);
    }

    private void validateCreateExpenseexpenseDto(ExpenseDto expenseDto) {
        if (expenseDto.getDescription() == null || expenseDto.getDescription().trim().isEmpty()) {
            throw new InvalidExpenseDataException("Description is required");
        }

        if (expenseDto.getAmount() == null || expenseDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidExpenseDataException("Amount must be greater than zero");
        }

        if (expenseDto.getGroupId() == null) {
            throw new InvalidExpenseDataException("Group ID is required");
        }

        if (expenseDto.getPaidByUserId() == null) {
            throw new InvalidExpenseDataException("Paid by user ID is required");
        }

        if (expenseDto.getSplitType() == null) {
            throw new InvalidExpenseDataException("Split type is required");
        }

        SplitType splitType;
        try {
            splitType = SplitType.valueOf(expenseDto.getSplitType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidExpenseDataException("Invalid split type. Must be EQUAL or UNEQUAL");
        }

        if (splitType == SplitType.UNEQUAL) {
            validateUnequalSplit(expenseDto);
        }
    }

    private void validateUnequalSplit(ExpenseDto expenseDto) {
        if (expenseDto.getUserShares() == null || expenseDto.getUserShares().isEmpty()) {
            throw new InvalidExpenseDataException("User shares are required for unequal split");
        }

        // Validate that shares sum up to the total amount
        BigDecimal totalShares = expenseDto.getUserShares().stream()
                .map(UserShareDto::getShare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalShares.compareTo(expenseDto.getAmount()) != 0) {
            throw new InvalidExpenseDataException("Sum of user shares must equal the total expense amount");
        }

        // Validate that all shares are positive
        boolean hasNegativeShare = expenseDto.getUserShares().stream()
                .anyMatch(share -> share.getShare().compareTo(BigDecimal.ZERO) <= 0);

        if (hasNegativeShare) {
            throw new InvalidExpenseDataException("All user shares must be positive");
        }
    }

    private List<ExpenseShare> createExpenseShares(Expense expense, ExpenseDto expenseDto, Group group) {
        List<ExpenseShare> expenseShares = new ArrayList<>();

        if (SplitType.EQUAL.name().equalsIgnoreCase(expenseDto.getSplitType())) {
            // Equal split among all group users
            BigDecimal sharePerUser = expenseDto.getAmount()
                    .divide(BigDecimal.valueOf(group.getUsers().size()), 2, RoundingMode.HALF_UP);

            for (User user : group.getUsers()) {
                ExpenseShare expenseShare = ExpenseShare.builder()
                        .expense(expense)
                        .user(user)
                        .share(sharePerUser)
                        .build();
                expenseShares.add(expenseShare);
            }
        } else {
            // Unequal split
            for (UserShareDto userShare : expenseDto.getUserShares()) {
                User user = userRepository.findById(userShare.getUserId())
                        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userShare.getUserId()));

                // Validate that user is part of the group
                if (!group.getUsers().contains(user)) {
                    throw new InvalidExpenseDataException("User with id " + userShare.getUserId() + " is not part of the group");
                }

                ExpenseShare expenseShare = ExpenseShare.builder()
                        .expense(expense)
                        .user(user)
                        .share(userShare.getShare())
                        .build();
                expenseShares.add(expenseShare);
            }
        }

        return expenseShares;
    }

    private ExpenseResponseDto mapToExpenseResponse(Expense expense) {
        List<ExpenseShareResponseDto> shares = expense.getExpenseShares().stream()
                .map(share -> ExpenseShareResponseDto.builder()
                        .userId(share.getUser().getId())
                        .userName(share.getUser().getUserName())
                        .share(share.getShare())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponseDto.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .groupId(expense.getGroup().getId())
                .groupName(expense.getGroup().getName())
                .paidByUserId(expense.getPaidBy().getId())
                .paidByUserName(expense.getPaidBy().getUserName())
                .splitType(expense.getSplitType().name())
                .createdAt(expense.getCreatedAt())
                .shares(shares)
                .build();
    }
}

