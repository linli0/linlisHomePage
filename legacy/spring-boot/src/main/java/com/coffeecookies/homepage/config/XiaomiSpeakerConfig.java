package com.coffeecookies.homepage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "xiaomi.speaker")
public class XiaomiSpeakerConfig {
    
    private String deviceIp;
    private String deviceToken;
    private String deviceModel = "LX06";
    private String miAccountUsername;
    private String miAccountPassword;
    private boolean enabled = false;
}