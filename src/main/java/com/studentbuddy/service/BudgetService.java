package com.studentbuddy.service;

import com.studentbuddy.dto.BudgetRequest;
import com.studentbuddy.dto.BudgetResponse;
import com.studentbuddy.exception.ResourceNotFoundException;
import com.studentbuddy.model.Budget;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for budgets. Every method is scoped to a specific user, so a
 * student can only ever read or modify their own budgets.
 */
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public List<BudgetResponse> listFor(User user) {
        return budgetRepository.findByUserId(user.getId()).stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse getFor(User user, Long id) {
        return BudgetResponse.from(requireOwned(user, id));
    }

    @Transactional
    public BudgetResponse create(User user, BudgetRequest request) {
        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(request.category());
        budget.setAmount(request.amount());
        return BudgetResponse.from(budgetRepository.save(budget));
    }

    @Transactional
    public BudgetResponse update(User user, Long id, BudgetRequest request) {
        Budget budget = requireOwned(user, id);
        budget.setCategory(request.category());
        budget.setAmount(request.amount());
        return BudgetResponse.from(budgetRepository.save(budget));
    }

    @Transactional
    public void delete(User user, Long id) {
        Budget budget = requireOwned(user, id);
        budgetRepository.delete(budget);
    }

    /** Loads a budget only if it belongs to the user; otherwise 404. */
    private Budget requireOwned(User user, Long id) {
        return budgetRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Budget", id));
    }


    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }
}