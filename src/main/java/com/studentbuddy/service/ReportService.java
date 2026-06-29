package com.studentbuddy.service;

import com.studentbuddy.dto.CategoryAmount;
import com.studentbuddy.dto.MonthlyReportResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.CategorySpending;
import com.studentbuddy.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates spending reports. Currently a monthly, per-category breakdown
 * (the equivalent of the MonthlySpendingReport stored procedure, kept in the
 * service layer to match the project's clean-architecture rules).
 */
@Service
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public MonthlyReportResponse monthlyReport(User user, Integer year, Integer month) {
        // Default to the current month when the caller omits the parameters.
        YearMonth target = (year != null && month != null)
                ? YearMonth.of(year, month)        // throws DateTimeException if month is out of range
                : YearMonth.now(clock);

        LocalDate start = target.atDay(1);
        LocalDate end = target.atEndOfMonth();

        List<CategorySpending> rows =
                expenseRepository.findSpendingByCategory(user.getId(), start, end);

        BigDecimal total = rows.stream()
                .map(CategorySpending::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryAmount> breakdown = new ArrayList<>();
        for (CategorySpending row : rows) {
            breakdown.add(new CategoryAmount(
                    row.getCategory(),
                    row.getTotal(),
                    percentageOf(row.getTotal(), total)));
        }

        return new MonthlyReportResponse(
                target.getYear(), target.getMonthValue(), total, breakdown);
    }

    /** Share of the total as a percentage, to one decimal place. 0 if total is 0. */
    private BigDecimal percentageOf(BigDecimal part, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }


    public ReportService(ExpenseRepository expenseRepository, Clock clock) {
        this.expenseRepository = expenseRepository;
        this.clock = clock;
    }
}