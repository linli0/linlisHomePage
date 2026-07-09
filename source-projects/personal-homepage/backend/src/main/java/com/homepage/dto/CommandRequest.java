package com.homepage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandRequest {

    @NotBlank(message = "Prompt cannot be empty")
    private String prompt;

    private String context;
    private String model;
    private Boolean stream = false;
}
