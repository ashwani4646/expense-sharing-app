package com.expenseshare.demo.dto;

import com.expenseshare.demo.entity.UserBalance;
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
public class GroupBalanceSummary {
    private Long groupId;
    private String groupName;
    private List<UserBalance> balances;
    private BigDecimal totalGroupDebt;
    private String message;
}
