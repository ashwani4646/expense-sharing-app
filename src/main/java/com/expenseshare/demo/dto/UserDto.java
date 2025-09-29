package com.expenseshare.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    Long id;
    @NotNull(message = "First Name is required")
    String firstName;
    String lastName;
    @NotNull(message = "User Name is required")
    String userName;
    @NotNull(message = "Email Id is required")
    String emailId;
    @NotNull(message = "Password is required")
    String password;
    @NotNull
    String role;
}
