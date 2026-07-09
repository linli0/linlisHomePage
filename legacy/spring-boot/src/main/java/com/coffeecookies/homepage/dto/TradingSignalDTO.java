package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingSignalDTO {
    
    private Long id;
    private Long strategyId;
    private String strategyName;
    private String symbol;
    private String signalType;
    private BigDecimal price;
    private BigDecimal targetPrice;
    private BigDecimal stopLoss;
    private BigDecimal confidence;
    private String reason;
    private Boolean executed;
    private LocalDateTime executedAt;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalSummary {
        private Integer buyCount;
        private Integer sellCount;
        private Integer holdCount;
        private BigDecimal avgConfidence;
    }
}