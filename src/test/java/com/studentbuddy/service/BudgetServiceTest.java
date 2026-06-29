package com.studentbuddy.service;

import com.studentbuddy.dto.BudgetRequest;
import com.studentbuddy.dto.BudgetResponse;
import com.studentbuddy.exception.ResourceNotFoundException;
import com.studentbuddy.model.Budget;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("ada@example.com");
    }

    @Test
    void create_savesBudgetForUser() {
        BudgetRequest request = new BudgetRequest("Food", new BigDecimal("200.00"));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> {
            Budget b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BudgetResponse response = budgetService.create(user, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.category()).isEqualTo("Food");
        assertThat(response.amount()).isEqualByComparingTo("200.00");
    }

    @Test
    void getFor_whenNotOwned_throwsNotFound() {
        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getFor(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_whenNotOwned_throwsAndDoesNotDelete() {
        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.delete(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(budgetRepository, never()).delete(any());
    }

    @Test
    void update_modifiesOwnedBudget() {
        Budget existing = new Budget();
        existing.setId(5L);
        existing.setUser(user);
        existing.setCategory("Food");
        existing.setAmount(new BigDecimal("100.00"));
        when(budgetRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existing));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        BudgetResponse response = budgetService.update(
                user, 5L, new BudgetRequest("Groceries", new BigDecimal("150.00")));

        assertThat(response.category()).isEqualTo("Groceries");
        assertThat(response.amount()).isEqualByComparingTo("150.00");
    }
}
