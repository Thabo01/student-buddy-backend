package com.studentbuddy.service;

import com.studentbuddy.dto.ExpenseRequest;
import com.studentbuddy.dto.ExpenseResponse;
import com.studentbuddy.exception.ResourceNotFoundException;
import com.studentbuddy.model.Expense;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void create_persistsExpenseWithAllFields() {
        ExpenseRequest request = new ExpenseRequest(
                "Food", "Lunch", new BigDecimal("8.50"), LocalDate.of(2025, 6, 14));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> {
            Expense e = inv.getArgument(0);
            e.setId(3L);
            return e;
        });

        ExpenseResponse response = expenseService.create(user, request);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.category()).isEqualTo("Food");
        assertThat(response.description()).isEqualTo("Lunch");
        assertThat(response.amount()).isEqualByComparingTo("8.50");
        assertThat(response.expenseDate()).isEqualTo(LocalDate.of(2025, 6, 14));
    }

    @Test
    void getFor_whenNotOwned_throwsNotFound() {
        when(expenseRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getFor(user, 50L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
