package com.expenseshare.demo.repository;

import com.expenseshare.demo.entity.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {
}
