package com.studentbuddy.model;

/**
 * Categories of derived notification. These are computed on the fly from the
 * user's budgets, expenses, and tasks — they are not stored in the database.
 */
public enum NotificationType {
    BUDGET_EXCEEDED,
    BUDGET_NEAR_LIMIT,
    TASK_OVERDUE,
    TASK_DUE_SOON
}
