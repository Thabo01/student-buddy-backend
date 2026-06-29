package com.studentbuddy.exception;

/**
 * Thrown for client errors that should map to HTTP 400, e.g. supplying the
 * wrong current password when changing it.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
