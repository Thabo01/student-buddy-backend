package com.studentbuddy.service;

import com.studentbuddy.dto.NotificationResponse;
import com.studentbuddy.model.Budget;
import com.studentbuddy.model.NotificationType;
import com.studentbuddy.model.Task;
import com.studentbuddy.model.TaskStatus;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.BudgetRepository;
import com.studentbuddy.repository.CategorySpending;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private TaskRepository taskRepository;

    private NotificationService notificationService;
    private User user;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-06-15T10:00:00Z"), ZoneId.of("UTC"));
        notificationService = new NotificationService(
                budgetRepository, expenseRepository, taskRepository, fixedClock);
        user = new User();
        user.setId(1L);
    }

    private static CategorySpending spending(String category, String total) {
        return new CategorySpending() {
            public String getCategory() { return category; }
            public BigDecimal getTotal() { return new BigDecimal(total); }
        };
    }

    @Test
    void flagsExceededBudget() {
        Budget food = new Budget();
        food.setCategory("Food");
        food.setAmount(new BigDecimal("100.00"));
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(food));
        when(expenseRepository.findSpendingByCategory(anyLong(), any(), any()))
                .thenReturn(List.of(spending("Food", "150.00")));
        when(taskRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<NotificationResponse> result = notificationService.getFor(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.BUDGET_EXCEEDED);
    }

    @Test
    void flagsOverdueTask() {
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of());
        when(expenseRepository.findSpendingByCategory(anyLong(), any(), any()))
                .thenReturn(List.of());
        Task overdue = new Task();
        overdue.setTitle("Submit report");
        overdue.setStatus(TaskStatus.TODO);
        overdue.setDueDate(LocalDate.of(2025, 6, 10)); // before the fixed 'today'
        when(taskRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(overdue));

        List<NotificationResponse> result = notificationService.getFor(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(NotificationType.TASK_OVERDUE);
    }

    @Test
    void doneTasksProduceNoNotification() {
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of());
        when(expenseRepository.findSpendingByCategory(anyLong(), any(), any()))
                .thenReturn(List.of());
        Task done = new Task();
        done.setTitle("Old task");
        done.setStatus(TaskStatus.DONE);
        done.setDueDate(LocalDate.of(2025, 6, 1));
        when(taskRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(done));

        List<NotificationResponse> result = notificationService.getFor(user);

        assertThat(result).isEmpty();
    }
}
