package com.studentbuddy.controller;

import com.studentbuddy.dto.BudgetRequest;
import com.studentbuddy.dto.BudgetResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.service.BudgetService;
import com.studentbuddy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CRUD endpoints for budgets. All routes require authentication (enforced
 * globally) and operate only on the current user's data.
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> list() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(budgetService.listFor(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> get(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(budgetService.getFor(user, id));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        User user = userService.getCurrentUser();
        BudgetResponse created = budgetService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(budgetService.update(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        budgetService.delete(user, id);
        return ResponseEntity.noContent().build();
    }


    public BudgetController(BudgetService budgetService, UserService userService) {
        this.budgetService = budgetService;
        this.userService = userService;
    }
}