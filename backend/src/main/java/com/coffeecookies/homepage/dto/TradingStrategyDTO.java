package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingStrategyDTO {
    
    private Long id;
    private String name;
    private String description;
    private String type;
    private String status;
    private String parameters;
    private String symbol;
    private BigDecimal totalReturn;
    private BigDecimal sharpeRatio;
    private BigDecimal maxDrawdown;
    private BigDecimal winRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterInfo {
        private String key;
        private String value;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private BigDecimal totalReturn;
        private BigDecimal sharpeRatio;
        private BigDecimal maxDrawdown;
        private BigDecimal winRate;
        private Integer totalTrades;
    }
}