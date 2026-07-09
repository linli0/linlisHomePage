package com.coffeecookies.homepage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    private String username; // Optional for password-only mode
    
    @NotBlank(message = "Password is required")
    private String password;
}
