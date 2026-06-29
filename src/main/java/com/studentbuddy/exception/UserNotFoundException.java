package com.studentbuddy.exception;

/**
 * Thrown when a user lookup fails (e.g. the authenticated principal no longer
 * exists in the database).
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
