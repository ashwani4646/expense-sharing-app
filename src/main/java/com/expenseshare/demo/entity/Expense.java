package com.expenseshare.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity(name = "expenses")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long expenseId;

    @Column
    BigDecimal amount;

    @Column
    String createdBy;

    @Column
    String groupId;


}
