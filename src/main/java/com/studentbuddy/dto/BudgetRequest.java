package com.studentbuddy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Incoming payload for creating or updating a budget.
 */
public record BudgetRequest(

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount
) {
}
