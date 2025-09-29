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
public class SettlementGroupDetailDto {
    private Long groupId;
    private String groupName;
    private BigDecimal amountSettled;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
}
