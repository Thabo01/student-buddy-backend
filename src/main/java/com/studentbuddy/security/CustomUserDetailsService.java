package com.studentbuddy.security;

import com.studentbuddy.model.User;
import com.studentbuddy.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Bridges our {@link User} entity to Spring Security's authentication system.
 * We use email as the username. The entity is kept free of security concerns;
 * here we adapt it into a Spring Security {@link UserDetails}.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // already a BCrypt hash
                .authorities(Collections.emptyList())
                .build();
    }


    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}