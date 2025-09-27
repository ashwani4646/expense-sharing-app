package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.Expense;
import com.expenseshare.demo.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {


}
