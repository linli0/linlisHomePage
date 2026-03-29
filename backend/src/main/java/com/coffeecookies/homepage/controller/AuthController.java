package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.*;
import com.coffeecookies.homepage.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok(Result.success(authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<Result<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(Result.success(authService.register(request)));
    }

    @GetMapping("/me")
    public ResponseEntity<Result<UserDTO>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Result.error(401, "Not authenticated"));
        }
        return ResponseEntity.ok(Result.success(authService.getCurrentUser(userDetails.getUsername())));
    }
}
