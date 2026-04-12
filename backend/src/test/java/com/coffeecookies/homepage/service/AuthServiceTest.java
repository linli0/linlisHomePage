package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.BaseIntegrationTest;
import com.coffeecookies.homepage.dto.LoginRequest;
import com.coffeecookies.homepage.entity.User;
import com.coffeecookies.homepage.entity.Role;
import com.coffeecookies.homepage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuthService 单元测试
 */
class AuthServiceTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldAuthenticateUserWithValidPassword() {
        // Given
        User user = TestDataBuilder.user()
                .username("testuser")
                .password("admin123") // Using default password from application
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("admin123");

        // When
        var authResponse = authService.login(loginRequest);

        // Then
        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getUsername()).isEqualTo("testuser");
        assertThat(authResponse.getToken()).isNotBlank();
        assertThat(authResponse.getRole()).isEqualTo(Role.USER.name());
    }

    @Test
    void shouldThrowBadCredentialsExceptionForInvalidPassword() {
        // Given
        User user = TestDataBuilder.user()
                .username("testuser")
                .password("admin123")
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void shouldAuthenticateAdminUser() {
        // Given
        User admin = TestDataBuilder.adminUser();
        admin.setPassword("admin123"); // Using default admin password
        userRepository.save(admin);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        // When
        var authResponse = authService.login(loginRequest);

        // Then
        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getUsername()).isEqualTo("admin");
        assertThat(authResponse.getRole()).isEqualTo(Role.ADMIN.name());
    }
}