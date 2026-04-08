package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.config.SiteConfig;
import com.coffeecookies.homepage.dto.*;
import com.coffeecookies.homepage.entity.User;
import com.coffeecookies.homepage.repository.UserRepository;
import com.coffeecookies.homepage.security.JwtUtils;
import com.coffeecookies.homepage.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SiteConfig siteConfig;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Handle traditional username/password login
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            return AuthResponse.builder()
                    .token(jwt)
                    .type("Bearer")
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .displayName(user.getDisplayName())
                    .avatar(user.getAvatar())
                    .role(user.getRole().name())
                    .expiresIn(86400000L)
                    .build();
        } else {
            // Handle password-only login (Express compatibility mode)
            return loginWithPasswordOnly(request.getPassword());
        }
    }

    @Transactional
    public AuthResponse loginWithPasswordOnly(String password) {
        // Try to authenticate with admin password first
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser != null && passwordEncoder.matches(password, adminUser.getPassword())) {
            // Create authentication token for admin
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin", password, UserDetailsImpl.build(adminUser).getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            return AuthResponse.builder()
                    .token(jwt)
                    .type("Bearer")
                    .id(adminUser.getId())
                    .username(adminUser.getUsername())
                    .email(adminUser.getEmail())
                    .displayName(adminUser.getDisplayName())
                    .avatar(adminUser.getAvatar())
                    .role(adminUser.getRole().name())
                    .expiresIn(86400000L)
                    .build();
        }
        
        // Try user password
        User userUser = userRepository.findByUsername("user").orElse(null);
        if (userUser != null && passwordEncoder.matches(password, userUser.getPassword())) {
            // Create authentication token for user
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user", password, UserDetailsImpl.build(userUser).getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            return AuthResponse.builder()
                    .token(jwt)
                    .type("Bearer")
                    .id(userUser.getId())
                    .username(userUser.getUsername())
                    .email(userUser.getEmail())
                    .displayName(userUser.getDisplayName())
                    .avatar(userUser.getAvatar())
                    .role(userUser.getRole().name())
                    .expiresIn(86400000L)
                    .build();
        }
        
        throw new RuntimeException("Invalid password");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Registration is disabled - matches Express behavior
        throw new RuntimeException("Registration is disabled");
    }

    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    @Transactional
    public UserDTO updateProfile(String username, ProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getEmail() != null) {
            // Check if email is already taken by another user
            userRepository.findByEmail(request.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(user.getId())) {
                        throw new RuntimeException("Email already exists");
                    }
                });
            user.setEmail(request.getEmail());
        }
        
        userRepository.save(user);
        
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
    
    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Set new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
