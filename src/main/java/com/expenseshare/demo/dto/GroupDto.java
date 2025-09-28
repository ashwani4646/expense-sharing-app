package com.expenseshare.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
public class GroupDto {

    Long id;

    @NotNull(message = "Name is required")
    String name;
}
