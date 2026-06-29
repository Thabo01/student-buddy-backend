package com.studentbuddy.service;

import com.studentbuddy.dto.ChangePasswordRequest;
import com.studentbuddy.dto.UpdateProfileRequest;
import com.studentbuddy.dto.UserResponse;
import com.studentbuddy.exception.BadRequestException;
import com.studentbuddy.exception.EmailAlreadyExistsException;
import com.studentbuddy.model.User;
import com.studentbuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Ada Lovelace");
        user.setEmail("ada@example.com");
        user.setPassword("hashed-current");
    }

    @Test
    void updateProfile_changesNameAndEmail() {
        when(userRepository.existsByEmail("ada.l@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse res = userService.updateProfile(
                user, new UpdateProfileRequest("Ada L", "ada.l@example.com"));

        assertThat(res.fullName()).isEqualTo("Ada L");
        assertThat(res.email()).isEqualTo("ada.l@example.com");
    }

    @Test
    void updateProfile_whenEmailTakenByAnother_throws() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(
                user, new UpdateProfileRequest("Ada", "taken@example.com")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_keepingSameEmail_doesNotTripUniquenessCheck() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse res = userService.updateProfile(
                user, new UpdateProfileRequest("Ada New", "ada@example.com"));

        assertThat(res.fullName()).isEqualTo("Ada New");
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void changePassword_withCorrectCurrent_encodesAndSaves() {
        when(passwordEncoder.matches("current", "hashed-current")).thenReturn(true);
        when(passwordEncoder.encode("newpassword1")).thenReturn("hashed-new");

        userService.changePassword(user, new ChangePasswordRequest("current", "newpassword1"));

        assertThat(user.getPassword()).isEqualTo("hashed-new");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_withWrongCurrent_throwsAndDoesNotSave() {
        when(passwordEncoder.matches("wrong", "hashed-current")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(
                user, new ChangePasswordRequest("wrong", "newpassword1")))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }
}
