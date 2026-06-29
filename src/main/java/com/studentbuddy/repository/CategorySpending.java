package com.studentbuddy.repository;

import java.math.BigDecimal;

/**
 * Read-only projection returned by the grouped spending query. Spring Data
 * maps the query's {@code category} and {@code total} aliases to these getters.
 */
public interface CategorySpending {
    String getCategory();

    BigDecimal getTotal();
}
