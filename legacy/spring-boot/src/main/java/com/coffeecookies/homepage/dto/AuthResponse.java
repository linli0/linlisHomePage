package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type;
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatar;
    private String role;
    private Long expiresIn;
}
