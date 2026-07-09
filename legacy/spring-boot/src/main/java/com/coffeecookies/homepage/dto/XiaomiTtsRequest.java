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
public class XiaomiTtsRequest {
    
    @NotBlank(message = "Text is required")
    private String text;
}