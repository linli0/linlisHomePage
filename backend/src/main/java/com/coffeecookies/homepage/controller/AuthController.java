package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.*;
import com.coffeecookies.homepage.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(Result.success(authService.login(request)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(HttpStatus.UNAUTHORIZED.value(), "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Result<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(HttpStatus.FORBIDDEN.value(), "Registration is disabled"));
    }

    @GetMapping("/me")
    public ResponseEntity<Result<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(HttpStatus.UNAUTHORIZED.value(), "Not authenticated"));
        }
        return ResponseEntity.ok(Result.success(authService.getCurrentUser(userDetails.getUsername())));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<Result<UserDTO>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(HttpStatus.UNAUTHORIZED.value(), "Not authenticated"));
        }
        try {
            UserDTO updatedUser = authService.updateProfile(userDetails.getUsername(), request);
            return ResponseEntity.ok(Result.success(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Result.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }
    
    @PutMapping("/password")
    public ResponseEntity<Result<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(HttpStatus.UNAUTHORIZED.value(), "Not authenticated"));
        }
        try {
            authService.changePassword(userDetails.getUsername(), request);
            return ResponseEntity.ok(Result.success("Password changed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
        }
    }
}
