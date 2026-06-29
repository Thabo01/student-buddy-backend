package com.studentbuddy.controller;

import com.studentbuddy.dto.AuthResponse;
import com.studentbuddy.dto.LoginRequest;
import com.studentbuddy.dto.RegisterRequest;
import com.studentbuddy.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints. These are the only routes left public by
 * SecurityConfig; everything else requires a valid JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /** Registers a new student and returns a JWT. 201 Created on success. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Logs an existing student in and returns a JWT. 200 OK on success. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }


    public AuthController(AuthService authService) {
        this.authService = authService;
    }
}