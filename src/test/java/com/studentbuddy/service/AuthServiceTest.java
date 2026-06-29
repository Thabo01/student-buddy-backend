package com.studentbuddy.service;

import com.studentbuddy.dto.AuthResponse;
import com.studentbuddy.dto.LoginRequest;
import com.studentbuddy.dto.RegisterRequest;
import com.studentbuddy.exception.EmailAlreadyExistsException;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.UserRepository;
import com.studentbuddy.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService. Collaborators are mocked so we test only the
 * service's own logic — no database or Spring context required.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_savesUserWithHashedPassword_andReturnsToken() {
        RegisterRequest request = new RegisterRequest("Ada Lovelace", "ada@example.com", "password123");

        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken("ada@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.userId()).isEqualTo(1L);

        // Verify the stored password was the hash, never the plaintext.
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_whenEmailExists_throwsAndDoesNotSave() {
        RegisterRequest request = new RegisterRequest("Ada", "ada@example.com", "password123");
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("ada@example.com", "password123");

        User user = new User();
        user.setId(7L);
        user.setEmail("ada@example.com");
        user.setFullName("Ada Lovelace");

        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("ada@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(7L);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_withBadCredentials_propagatesException() {
        LoginRequest request = new LoginRequest("ada@example.com", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(anyString());
    }
}
