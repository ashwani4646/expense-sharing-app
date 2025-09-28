package com.expenseshare.demo.dto;

import com.expenseshare.demo.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRoleDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role is required")
    private Role role;
}
