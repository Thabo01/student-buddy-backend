package com.studentbuddy.service;

import com.studentbuddy.dto.NotificationResponse;
import com.studentbuddy.model.Budget;
import com.studentbuddy.model.NotificationSeverity;
import com.studentbuddy.model.NotificationType;
import com.studentbuddy.model.Task;
import com.studentbuddy.model.TaskStatus;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.BudgetRepository;
import com.studentbuddy.repository.CategorySpending;
import com.studentbuddy.repository.ExpenseRepository;
import com.studentbuddy.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Produces notifications on demand from the user's live data. Nothing is
 * persisted: each call recomputes the current alerts.
 *
 * Rules:
 *  - Budget exceeded: this month's spend in a category exceeds its budget.
 *  - Budget near limit: spend is at/above 80% of the budget (but not over).
 *  - Task overdue: an unfinished task's due date is in the past.
 *  - Task due soon: an unfinished task is due within the next 3 days.
 */
@Service
public class NotificationService {

    private static final BigDecimal NEAR_LIMIT_RATIO = new BigDecimal("0.80");
    private static final int DUE_SOON_DAYS = 3;

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final TaskRepository taskRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getFor(User user) {
        List<NotificationResponse> notifications = new ArrayList<>();
        notifications.addAll(budgetNotifications(user));
        notifications.addAll(taskNotifications(user));
        return notifications;
    }

    private List<NotificationResponse> budgetNotifications(User user) {
        Long userId = user.getId();
        YearMonth month = YearMonth.now(clock);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        // Sum this month's spend per category.
        Map<String, BigDecimal> spentByCategory = new HashMap<>();
        for (CategorySpending row : expenseRepository.findSpendingByCategory(userId, start, end)) {
            spentByCategory.put(row.getCategory(), row.getTotal());
        }

        List<NotificationResponse> result = new ArrayList<>();
        for (Budget budget : budgetRepository.findByUserId(userId)) {
            BigDecimal spent = spentByCategory.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
            BigDecimal limit = budget.getAmount();

            if (spent.compareTo(limit) > 0) {
                result.add(new NotificationResponse(
                        NotificationType.BUDGET_EXCEEDED,
                        NotificationSeverity.DANGER,
                        "You've exceeded your %s budget (%s spent of %s)."
                                .formatted(budget.getCategory(), spent, limit)));
            } else if (limit.signum() > 0
                    && spent.compareTo(limit.multiply(NEAR_LIMIT_RATIO)) >= 0) {
                result.add(new NotificationResponse(
                        NotificationType.BUDGET_NEAR_LIMIT,
                        NotificationSeverity.WARNING,
                        "You're close to your %s budget (%s spent of %s)."
                                .formatted(budget.getCategory(), spent, limit)));
            }
        }
        return result;
    }

    private List<NotificationResponse> taskNotifications(User user) {
        LocalDate today = LocalDate.now(clock);
        LocalDate soonCutoff = today.plusDays(DUE_SOON_DAYS);

        List<NotificationResponse> result = new ArrayList<>();
        for (Task task : taskRepository.findByUserIdOrderByCreatedAtDesc(user.getId())) {
            if (task.getStatus() == TaskStatus.DONE || task.getDueDate() == null) {
                continue;
            }
            LocalDate due = task.getDueDate();
            if (due.isBefore(today)) {
                result.add(new NotificationResponse(
                        NotificationType.TASK_OVERDUE,
                        NotificationSeverity.DANGER,
                        "Task \"%s\" was due on %s.".formatted(task.getTitle(), due)));
            } else if (!due.isAfter(soonCutoff)) {
                result.add(new NotificationResponse(
                        NotificationType.TASK_DUE_SOON,
                        NotificationSeverity.INFO,
                        "Task \"%s\" is due on %s.".formatted(task.getTitle(), due)));
            }
        }
        return result;
    }


    public NotificationService(BudgetRepository budgetRepository, ExpenseRepository expenseRepository, TaskRepository taskRepository, Clock clock) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.taskRepository = taskRepository;
        this.clock = clock;
    }
}