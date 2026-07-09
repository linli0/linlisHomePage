package com.coffeecookies.homepage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XiaomiAiChatRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private Boolean stream = true;
}