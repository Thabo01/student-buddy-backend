package com.studentbuddy.controller;

import com.studentbuddy.dto.MonthlyReportResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.service.ReportService;
import com.studentbuddy.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reporting endpoints. {@code year}/{@code month} are optional; when omitted
 * the report defaults to the current month. The @Min/@Max constraints reject
 * nonsensical months with a 400.
 */
@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> monthly(
            @RequestParam(required = false) @Min(2000) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(reportService.monthlyReport(user, year, month));
    }


    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }
}