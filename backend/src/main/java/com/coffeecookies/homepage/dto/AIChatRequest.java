package com.coffeecookies.homepage.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class AIChatRequest {
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotBlank(message = "Prompt is required")
    private String prompt;
    
    private Boolean stream = true;
    
    private List<Integer> context;
}