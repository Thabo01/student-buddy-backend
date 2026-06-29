package com.studentbuddy.service;

import com.studentbuddy.dto.ChangePasswordRequest;
import com.studentbuddy.dto.UpdateProfileRequest;
import com.studentbuddy.dto.UserResponse;
import com.studentbuddy.exception.BadRequestException;
import com.studentbuddy.exception.EmailAlreadyExistsException;
import com.studentbuddy.exception.UserNotFoundException;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User lookups and self-service account management (profile + password).
 *
 * The JWT filter stores the user's email as the security principal's name, so
 * here we read that name and load the matching entity. Centralising this means
 * every controller/service can ask "who is logged in?" the same way.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No user found with email: " + email));
    }

    /**
     * Returns the entity for whoever made the current request.
     * Throws if there is no authenticated user in the security context.
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("No authenticated user in the security context");
        }
        // For our JWT auth, getName() returns the email used as the principal.
        return getByEmail(authentication.getName());
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }

    /**
     * Updates the user's name and email. If the email changes, it must not
     * already belong to another account.
     */
    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        String newEmail = request.email().trim();
        if (!newEmail.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }
        user.setFullName(request.fullName().trim());
        user.setEmail(newEmail);
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Changes the password after verifying the current one. Note: because the
     * email (the JWT subject) is unchanged, the existing token stays valid.
     */
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
}