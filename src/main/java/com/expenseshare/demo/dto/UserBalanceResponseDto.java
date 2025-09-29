package com.expenseshare.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceResponseDto {
    private Long userId;
    private String userName;
    private BigDecimal totalOwed; // Amount this user owes to others
    private BigDecimal totalOwedBy; // Amount owed to this user by others
    private BigDecimal netBalance; // Positive = user owes, Negative = user is owed
    private List<GroupBalanceDetailDto> groupBalances;
}