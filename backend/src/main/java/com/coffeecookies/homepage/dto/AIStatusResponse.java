package com.coffeecookies.homepage.dto;

import lombok.Data;

@Data
public class AIStatusResponse {
    private String status;
    private String message;
    private String hint;
}