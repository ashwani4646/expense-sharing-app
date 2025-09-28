package com.expenseshare.demo.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDto {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Long groupId;
    private String groupName;
    private Long paidByUserId;
    private String paidByUserName;
    private String splitType;
    private LocalDateTime createdAt;
    private List<ExpenseShareResponseDto> shares;
}
