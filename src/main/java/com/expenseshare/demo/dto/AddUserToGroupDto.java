package com.expenseshare.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddUserToGroupDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Group ID is required")
    private Long groupId;
}
