package com.studentbuddy.dto;

import java.math.BigDecimal;

/**
 * One row of a spending breakdown: how much went to a category and what
 * percentage of the period's total that represents.
 */
public record CategoryAmount(
        String category,
        BigDecimal amount,
        BigDecimal percentage
) {
}
