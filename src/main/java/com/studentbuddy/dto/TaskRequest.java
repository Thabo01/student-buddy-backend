package com.studentbuddy.dto;

import com.studentbuddy.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Payload for creating or updating a task. {@code status} is optional on
 * create (defaults to TODO); on update it lets the frontend move cards between
 * Kanban columns.
 */
public record TaskRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        TaskStatus status,

        LocalDate dueDate
) {
}
