package com.studentbuddy.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * A monthly spending report: the total spent in the month and a per-category
 * breakdown (ordered highest-spend first).
 */
public record MonthlyReportResponse(
        int year,
        int month,
        BigDecimal totalSpent,
        List<CategoryAmount> breakdown
) {
}
