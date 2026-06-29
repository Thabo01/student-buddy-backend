package com.studentbuddy.service;

import com.studentbuddy.dto.AuthResponse;
import com.studentbuddy.dto.LoginRequest;
import com.studentbuddy.dto.RegisterRequest;
import com.studentbuddy.exception.EmailAlreadyExistsException;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.UserRepository;
import com.studentbuddy.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for authentication. Controllers stay thin; all the real work
 * (hashing, persistence, token issuing, credential checking) lives here.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new student: validates email uniqueness, hashes the password
     * with BCrypt, saves the user, and returns a signed JWT.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password())); // BCrypt hash

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getEmail());
        return AuthResponse.of(token, saved.getId(), saved.getFullName(), saved.getEmail());
    }

    /**
     * Authenticates an existing student. Delegates credential checking to
     * Spring Security's AuthenticationManager; a bad password raises
     * BadCredentialsException, handled globally as a 401.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        // Credentials are valid at this point; load the user for the response.
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found: " + request.email()));

        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.of(token, user.getId(), user.getFullName(), user.getEmail());
    }


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
}