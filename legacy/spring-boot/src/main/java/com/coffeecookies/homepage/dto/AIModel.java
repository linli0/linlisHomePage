package com.coffeecookies.homepage.dto;

import lombok.Data;

@Data
public class AIModel {
    private String name;
    private Long size;
    private String parameterSize;
    private String family;
    private String format;
    private String modifiedAt;
}