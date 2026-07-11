package com.coffeecookies.homepage.dto;

import lombok.Data;

@Data
public class OllamaModel {
    private String name;
    private Long size;
    private String modifiedAt;
    private ModelDetails details;
    
    @Data
    public static class ModelDetails {
        private String parameterSize;
        private String family;
        private String format;
    }
}