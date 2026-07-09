package com.coffeecookies.homepage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "site")
public class SiteConfig {
    
    private String password = "admin123";
}