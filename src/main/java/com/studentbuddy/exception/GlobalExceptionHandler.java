package com.studentbuddy.exception;

import com.studentbuddy.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates exceptions into clean, consistent JSON responses so controllers
 * never have to deal with try/catch and error formatting themselves.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean-validation failures (@Valid on request bodies) -> 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields are invalid",
                fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /** Constraint violations on @Validated query params -> 400. */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex) {
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /** Invalid dates (e.g. month 13) -> 400. */
    @ExceptionHandler(java.time.DateTimeException.class)
    public ResponseEntity<ApiError> handleDateTime(java.time.DateTimeException ex) {
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid date parameters");
        return ResponseEntity.badRequest().body(body);
    }

    /** Application-level bad input (e.g. wrong current password) -> 400. */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        ApiError body = ApiError.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /** Duplicate email at registration -> 409. */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex) {
        ApiError body = ApiError.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /** Lookup of a non-existent user -> 404. */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        ApiError body = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /** Missing or non-owned resource -> 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiError body = ApiError.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /** Wrong email/password at login -> 401. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        ApiError body = ApiError.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /** Anything unexpected -> 500 (without leaking internal details). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        ApiError body = ApiError.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
