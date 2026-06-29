package com.studentbuddy.dto;

import com.studentbuddy.model.User;

import java.time.Instant;

/**
 * Public view of a user's account. Never includes the password hash.
 */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getCreatedAt());
    }
}
