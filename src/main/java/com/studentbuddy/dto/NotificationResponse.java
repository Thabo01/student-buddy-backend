package com.studentbuddy.dto;

import com.studentbuddy.model.NotificationSeverity;
import com.studentbuddy.model.NotificationType;

/**
 * A single derived notification shown in the UI's notification area.
 */
public record NotificationResponse(
        NotificationType type,
        NotificationSeverity severity,
        String message
) {
}
