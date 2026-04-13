package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.BaseIntegrationTest;
import com.coffeecookies.homepage.dto.LoginRequest;
import com.coffeecookies.homepage.entity.User;
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

    @Test
    void shouldAuthenticateUserWithValidPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("user123");

        var authResponse = authService.login(loginRequest);

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getUsername()).isEqualTo("user");
        assertThat(authResponse.getToken()).isNotBlank();
        assertThat(authResponse.getRole()).isEqualTo(User.Role.USER.name());
    }

    @Test
    void shouldThrowBadCredentialsExceptionForInvalidPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("user");
        loginRequest.setPassword("wrongpassword");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldAuthenticateAdminUser() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin123");

        var authResponse = authService.login(loginRequest);

        assertThat(authResponse).isNotNull();
        assertThat(authResponse.getUsername()).isEqualTo("admin");
        assertThat(authResponse.getRole()).isEqualTo(User.Role.ADMIN.name());
    }
}
