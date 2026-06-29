package com.studentbuddy.dto;

/**
 * Response returned after a successful register or login.
 * Note we never return the password hash — only safe, public fields.
 */
public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String fullName,
        String email
) {
    public static AuthResponse of(String token, Long userId, String fullName, String email) {
        return new AuthResponse(token, "Bearer", userId, fullName, email);
    }
}
