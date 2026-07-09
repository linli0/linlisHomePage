package com.coffeecookies.homepage.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIModelsResponse {
    private List<AIModel> models;
}