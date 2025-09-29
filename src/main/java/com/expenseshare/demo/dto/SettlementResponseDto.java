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
public class SettlementResponseDto {
    private Long settlementId;
    private Long payerId;
    private String payerName;
    private Long receiverId;
    private String receiverName;
    private BigDecimal totalAmountSettled;
    private String status;
    private String description;
    private java.time.LocalDateTime settlementDate;
    private List<SettlementGroupDetailDto> groupDetails;
}
