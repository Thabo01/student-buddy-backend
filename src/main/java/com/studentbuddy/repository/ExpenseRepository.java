package com.studentbuddy.repository;

import com.studentbuddy.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    /** Total spent by a user within a date range (inclusive). 0 if none. */
    @Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM Expense e
            WHERE e.user.id = :userId
              AND e.expenseDate BETWEEN :start AND :end
            """)
    BigDecimal sumAmountByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    /** Spending grouped by category within a date range — powers the report. */
    @Query("""
            SELECT e.category AS category, SUM(e.amount) AS total FROM Expense e
            WHERE e.user.id = :userId
              AND e.expenseDate BETWEEN :start AND :end
            GROUP BY e.category
            ORDER BY total DESC
            """)
    List<CategorySpending> findSpendingByCategory(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
