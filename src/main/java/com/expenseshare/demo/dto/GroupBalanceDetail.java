package com.expenseshare.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class GroupBalanceDetail {
    private Long groupId;
    private String groupName;
    private List<IndividualBalance> balances;
}
