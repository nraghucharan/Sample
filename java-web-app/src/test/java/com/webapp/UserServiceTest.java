package com.webapp;

import com.webapp.model.User;
import com.webapp.repository.UserRepository;
import com.webapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("plaintext");
    }

    @Test
    void registerUser_success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User saved = userService.registerUser(testUser);

        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo("ROLE_USER");
        verify(userRepository).save(testUser);
    }

    @Test
    void registerUser_duplicateUsername_throws() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username already taken");
    }

    @Test
    void registerUser_duplicateEmail_throws() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already registered");
    }

    @Test
    void loadUserByUsername_found() {
        testUser.setPassword("hashed");
        testUser.setRole("ROLE_USER");
        testUser.setEnabled(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var userDetails = userService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("ghost"))
            .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
    }
}
