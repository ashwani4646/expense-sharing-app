package com.expenseshare.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class IndividualBalance {
    private Long otherUserId;
    private String otherUserName;
    private BigDecimal amount; // Positive = you owe them, Negative = they owe you
}