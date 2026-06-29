package com.studentbuddy.service;

import com.studentbuddy.dto.DashboardSummaryResponse;
import com.studentbuddy.model.TaskStatus;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.BudgetRepository;
import com.studentbuddy.repository.ExpenseRepository;
import com.studentbuddy.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Builds the dashboard summary for a given user by aggregating their budgets,
 * this month's expenses, and their tasks.
 *
 * Design notes:
 *  - totalBudget is the sum of the user's budget limits.
 *  - totalSpent is scoped to the CURRENT calendar month, which is the natural
 *    window for a student budgeting app; remainingBudget = budget - spent.
 *  - The Clock is injected so "current month" is deterministic in tests.
 */
@Service
public class DashboardService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final TaskRepository taskRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummaryFor(User user) {
        Long userId = user.getId();

        YearMonth thisMonth = YearMonth.now(clock);
        LocalDate monthStart = thisMonth.atDay(1);
        LocalDate monthEnd = thisMonth.atEndOfMonth();

        BigDecimal totalBudget = budgetRepository.sumAmountByUserId(userId);
        BigDecimal totalSpent = expenseRepository
                .sumAmountByUserIdAndDateRange(userId, monthStart, monthEnd);
        BigDecimal remainingBudget = totalBudget.subtract(totalSpent);

        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countByUserIdAndStatus(userId, TaskStatus.DONE);
        long pendingTasks = totalTasks - completedTasks;

        return new DashboardSummaryResponse(
                user.getId(),
                user.getFullName(),
                totalBudget,
                totalSpent,
                remainingBudget,
                totalTasks,
                completedTasks,
                pendingTasks);
    }


    public DashboardService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository, TaskRepository taskRepository, Clock clock) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.taskRepository = taskRepository;
        this.clock = clock;
    }
}