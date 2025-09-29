package com.expenseshare.demo.dto;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettleBalanceRequestDto {
    private Long payerId;
    private Long receiverId;
    private BigDecimal amount;
    private String description;
}
