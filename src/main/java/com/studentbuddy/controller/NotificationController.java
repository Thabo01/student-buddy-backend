package com.studentbuddy.controller;

import com.studentbuddy.dto.NotificationResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.service.NotificationService;
import com.studentbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Returns the current user's live notifications (budget and task alerts).
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(notificationService.getFor(user));
    }


    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }
}