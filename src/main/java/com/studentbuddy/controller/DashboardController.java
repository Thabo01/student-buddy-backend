package com.studentbuddy.controller;

import com.studentbuddy.dto.DashboardSummaryResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.service.DashboardService;
import com.studentbuddy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard endpoint. This is the first protected route: SecurityConfig
 * requires a valid JWT for anything outside /api/auth/**, so reaching this
 * handler means the caller is authenticated.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<DashboardSummaryResponse> getDashboard() {
        User currentUser = userService.getCurrentUser();
        DashboardSummaryResponse summary = dashboardService.getSummaryFor(currentUser);
        return ResponseEntity.ok(summary);
    }


    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }
}