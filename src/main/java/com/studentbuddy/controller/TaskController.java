package com.studentbuddy.controller;

import com.studentbuddy.dto.TaskRequest;
import com.studentbuddy.dto.TaskResponse;
import com.studentbuddy.dto.TaskStatusRequest;
import com.studentbuddy.model.User;
import com.studentbuddy.service.TaskService;
import com.studentbuddy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(taskService.listFor(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> get(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(taskService.getFor(user, id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        User user = userService.getCurrentUser();
        TaskResponse created = taskService.create(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(taskService.update(user, id, request));
    }

    /** Move a task between Kanban columns without rewriting the whole task. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusRequest request) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(taskService.changeStatus(user, id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userService.getCurrentUser();
        taskService.delete(user, id);
        return ResponseEntity.noContent().build();
    }


    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }
}