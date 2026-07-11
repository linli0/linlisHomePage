package com.coffeecookies.homepage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "features")
public class FeatureFlagsProperties {

    private Ai ai = new Ai();
    private Tweets tweets = new Tweets();
    private Quant quant = new Quant();
    private Xiaomi xiaomi = new Xiaomi();

    @Data
    public static class Ai {
        private boolean enabled = false;
    }

    @Data
    public static class Tweets {
        private boolean enabled = false;
    }

    @Data
    public static class Quant {
        private boolean enabled = false;
    }

    @Data
    public static class Xiaomi {
        private boolean enabled = false;
    }
}
