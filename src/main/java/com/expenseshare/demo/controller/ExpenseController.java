package com.expenseshare.demo.controller;

import com.expenseshare.demo.dto.ExpenseDto;
import com.expenseshare.demo.dto.ExpenseResponseDto;
import com.expenseshare.demo.services.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponseDto> createExpense(@RequestBody ExpenseDto expenseDto) {
        log.info("Creating expense request: {}", expenseDto);
        ExpenseResponseDto response = expenseService.createExpense(expenseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> getExpense(@PathVariable Long expenseId) {
        log.info("Fetching expense with id: {}", expenseId);
        ExpenseResponseDto response = expenseService.getExpenseById(expenseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expenses/group/{groupId}")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByGroup(@PathVariable Long groupId) {
        log.info("Fetching expenses for group id: {}", groupId);
        List<ExpenseResponseDto> responses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/expenses/user/{userId}")
    public ResponseEntity<List<ExpenseResponseDto>> getExpensesByUser(@PathVariable Long userId) {
        log.info("Fetching expenses for user id: {}", userId);
        List<ExpenseResponseDto> responses = expenseService.getExpensesByUser(userId);
        return ResponseEntity.ok(responses);
    }
}
