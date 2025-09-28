package com.expenseshare.demo.dto;

import com.expenseshare.demo.entity.Expense;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto {

    private String description;
    private BigDecimal amount;
    private Long groupId;
    private Long paidByUserId;
    private String splitType; // "EQUAL" or "UNEQUAL"
    private List<UserShareDto> userShares; // Only required for UNEQUAL split
}


