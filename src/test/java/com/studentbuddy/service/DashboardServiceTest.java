package com.studentbuddy.service;

import com.studentbuddy.dto.DashboardSummaryResponse;
import com.studentbuddy.model.TaskStatus;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.BudgetRepository;
import com.studentbuddy.repository.ExpenseRepository;
import com.studentbuddy.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private TaskRepository taskRepository;

    private DashboardService dashboardService;
    private User user;

    @BeforeEach
    void setUp() {
        // Fixed clock: 15 June 2025, so "this month" is always June 2025.
        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-06-15T10:00:00Z"), ZoneId.of("UTC"));
        dashboardService = new DashboardService(
                budgetRepository, expenseRepository, taskRepository, fixedClock);

        user = new User();
        user.setId(1L);
        user.setFullName("Grace Hopper");
    }

    @Test
    void getSummaryFor_aggregatesBudgetsExpensesAndTasks() {
        when(budgetRepository.sumAmountByUserId(1L)).thenReturn(new BigDecimal("500.00"));
        when(expenseRepository.sumAmountByUserIdAndDateRange(
                eq(1L), eq(LocalDate.of(2025, 6, 1)), eq(LocalDate.of(2025, 6, 30))))
                .thenReturn(new BigDecimal("120.00"));
        when(taskRepository.countByUserId(1L)).thenReturn(5L);
        when(taskRepository.countByUserIdAndStatus(1L, TaskStatus.DONE)).thenReturn(2L);

        DashboardSummaryResponse summary = dashboardService.getSummaryFor(user);

        assertThat(summary.totalBudget()).isEqualByComparingTo("500.00");
        assertThat(summary.totalSpent()).isEqualByComparingTo("120.00");
        assertThat(summary.remainingBudget()).isEqualByComparingTo("380.00");
        assertThat(summary.totalTasks()).isEqualTo(5L);
        assertThat(summary.completedTasks()).isEqualTo(2L);
        assertThat(summary.pendingTasks()).isEqualTo(3L);
    }
}
