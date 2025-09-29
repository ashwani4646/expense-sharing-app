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
class BalanceChangeDetail {
    private Long groupId;
    private String groupName;
    private Long debtorId;
    private String debtorName;
    private Long creditorId;
    private String creditorName;
    private BigDecimal amountChanged;
    private BigDecimal newBalance;
}
