package com.studentbuddy.service;

import com.studentbuddy.dto.ExpenseRequest;
import com.studentbuddy.dto.ExpenseResponse;
import com.studentbuddy.exception.ResourceNotFoundException;
import com.studentbuddy.model.Expense;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listFor(User user) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(user.getId()).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getFor(User user, Long id) {
        return ExpenseResponse.from(requireOwned(user, id));
    }

    @Transactional
    public ExpenseResponse create(User user, ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setUser(user);
        apply(expense, request);
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(User user, Long id, ExpenseRequest request) {
        Expense expense = requireOwned(user, id);
        apply(expense, request);
        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(User user, Long id) {
        expenseRepository.delete(requireOwned(user, id));
    }

    private void apply(Expense expense, ExpenseRequest request) {
        expense.setCategory(request.category());
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
    }

    private Expense requireOwned(User user, Long id) {
        return expenseRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Expense", id));
    }


    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
}