package com.studentbuddy.controller;

import com.studentbuddy.dto.ExpenseRequest;
import com.studentbuddy.dto.ExpenseResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.service.ExpenseService;
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

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(expenseService.listFor(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(expenseService.getFor(user, id));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        User user = userService.getCurrentUser();
        ExpenseResponse created = expenseService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(expenseService.update(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        expenseService.delete(user, id);
        return ResponseEntity.noContent().build();
    }


    public ExpenseController(ExpenseService expenseService, UserService userService) {
        this.expenseService = expenseService;
        this.userService = userService;
    }
}