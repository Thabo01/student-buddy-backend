package com.studentbuddy.dto;

import com.studentbuddy.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for the dedicated status-change endpoint (PATCH /api/tasks/{id}/status).
 */
public record TaskStatusRequest(

        @NotNull(message = "Status is required")
        TaskStatus status
) {
}
