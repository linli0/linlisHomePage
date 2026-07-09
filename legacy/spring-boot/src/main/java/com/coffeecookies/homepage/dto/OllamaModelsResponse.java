package com.coffeecookies.homepage.dto;

import lombok.Data;

import java.util.List;

@Data
public class OllamaModelsResponse {
    private List<OllamaModel> models;
}