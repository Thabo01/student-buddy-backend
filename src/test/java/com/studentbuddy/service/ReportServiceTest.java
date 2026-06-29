package com.studentbuddy.service;

import com.studentbuddy.dto.MonthlyReportResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.CategorySpending;
import com.studentbuddy.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    private ReportService reportService;
    private User user;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-06-15T10:00:00Z"), ZoneId.of("UTC"));
        reportService = new ReportService(expenseRepository, fixedClock);
        user = new User();
        user.setId(1L);
    }

    private static CategorySpending spending(String category, String total) {
        return new CategorySpending() {
            public String getCategory() { return category; }
            public BigDecimal getTotal() { return new BigDecimal(total); }
        };
    }

    @Test
    void monthlyReport_computesTotalAndPercentages() {
        when(expenseRepository.findSpendingByCategory(anyLong(), any(), any()))
                .thenReturn(List.of(
                        spending("Food", "75.00"),
                        spending("Transport", "25.00")));

        MonthlyReportResponse report = reportService.monthlyReport(user, 2025, 6);

        assertThat(report.year()).isEqualTo(2025);
        assertThat(report.month()).isEqualTo(6);
        assertThat(report.totalSpent()).isEqualByComparingTo("100.00");
        assertThat(report.breakdown()).hasSize(2);
        assertThat(report.breakdown().get(0).percentage()).isEqualByComparingTo("75.0");
        assertThat(report.breakdown().get(1).percentage()).isEqualByComparingTo("25.0");
    }

    @Test
    void monthlyReport_defaultsToCurrentMonthWhenParamsOmitted() {
        when(expenseRepository.findSpendingByCategory(
                1L, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30)))
                .thenReturn(List.of());

        MonthlyReportResponse report = reportService.monthlyReport(user, null, null);

        assertThat(report.year()).isEqualTo(2025);
        assertThat(report.month()).isEqualTo(6);
        assertThat(report.totalSpent()).isEqualByComparingTo("0");
        assertThat(report.breakdown()).isEmpty();
    }
}
