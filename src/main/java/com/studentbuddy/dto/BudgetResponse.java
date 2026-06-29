package com.studentbuddy.dto;

import com.studentbuddy.model.Budget;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outgoing representation of a budget. Note the owner is never exposed.
 */
public record BudgetResponse(
        Long id,
        String category,
        BigDecimal amount,
        Instant createdAt,
        Instant updatedAt
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getAmount(),
                budget.getCreatedAt(),
                budget.getUpdatedAt());
    }
}
