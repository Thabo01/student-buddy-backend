package com.studentbuddy.dto;

import java.math.BigDecimal;

/**
 * Aggregated snapshot shown on the dashboard.
 *
 * Money is represented with {@link BigDecimal} (never double) to avoid
 * floating-point rounding errors in financial figures.
 *
 * Budget/expense/task figures are wired up in Phases 4–6; until then they are
 * returned as zero so the endpoint and the frontend can be built against a
 * stable shape.
 */
public record DashboardSummaryResponse(
        Long userId,
        String fullName,
        BigDecimal totalBudget,
        BigDecimal totalSpent,
        BigDecimal remainingBudget,
        long totalTasks,
        long completedTasks,
        long pendingTasks
) {
}
