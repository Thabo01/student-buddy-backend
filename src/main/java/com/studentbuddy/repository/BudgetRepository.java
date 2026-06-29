package com.studentbuddy.repository;

import com.studentbuddy.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    /** Ownership-scoped lookup: returns empty if the budget isn't this user's. */
    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    /** Sum of all budget limits for a user (0 if none). Used by the dashboard. */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Budget b WHERE b.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);
}
