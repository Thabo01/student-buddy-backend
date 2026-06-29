package com.studentbuddy.controller;

import com.studentbuddy.dto.ChangePasswordRequest;
import com.studentbuddy.dto.UpdateProfileRequest;
import com.studentbuddy.dto.UserResponse;
import com.studentbuddy.model.User;
import com.studentbuddy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-service account endpoints for the logged-in user.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.getProfile(userService.getCurrentUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = userService.getCurrentUser();
        userService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }


    public UserController(UserService userService) {
        this.userService = userService;
    }
}