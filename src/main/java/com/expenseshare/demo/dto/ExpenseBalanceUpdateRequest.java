package com.expenseshare.demo.dto;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseBalanceUpdateRequest {
    private Long expenseId;
    private Long groupId;
    private Long paidByUserId;
    private BigDecimal totalAmount;
    private String splitType; // "EQUAL" or "UNEQUAL"
    private List<Long> groupUserIds; // For equal split
    private List<UserShareDto> userShares; // For unequal split
    private String description;
}
