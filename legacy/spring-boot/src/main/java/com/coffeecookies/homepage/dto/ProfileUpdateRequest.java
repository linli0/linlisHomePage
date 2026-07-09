package com.coffeecookies.homepage.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    
    private String displayName;
    
    @Email(message = "Email should be valid")
    private String email;
}