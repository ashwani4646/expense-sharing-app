package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroupId(Long groupId);

    @Query("SELECT e FROM Expense e JOIN e.expenseShares es WHERE es.user.id = :userId")
    List<Expense> findExpensesByUserId(@Param("userId") Long userId);
}
