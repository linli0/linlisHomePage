package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIChatRequest {
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotBlank(message = "Prompt is required")
    private String prompt;
    
    private Boolean stream = true;
    
    private List<Integer> context;
}
