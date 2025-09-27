package com.expenseshare.demo.services;

import com.expenseshare.demo.dto.ExpenseDto;
import com.expenseshare.demo.entity.Expense;
import com.expenseshare.demo.mapper.ExpenseMapper;
import com.expenseshare.demo.repository.ExpenseRepository;
import com.expenseshare.demo.repository.GroupRepository;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    public void addExpense(ExpenseDto expense) {
        expenseRepository.save(ExpenseMapper.INSTANCE.toEntity(expense));

    }
}
