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
public class BacktestResultDTO {
    
    private Long id;
    private Long strategyId;
    private String strategyName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal totalReturn;
    private BigDecimal sharpeRatio;
    private BigDecimal maxDrawdown;
    private BigDecimal winRate;
    private Integer totalTrades;
    private Integer profitTrades;
    private Integer lossTrades;
    private BigDecimal averageProfit;
    private BigDecimal averageLoss;
    private String parameters;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeStatistics {
        private Integer totalTrades;
        private Integer profitTrades;
        private Integer lossTrades;
        private BigDecimal winRate;
        private BigDecimal avgProfit;
        private BigDecimal avgLoss;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceChart {
        private List<DataPoint> data;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DataPoint {
            private LocalDateTime date;
            private BigDecimal value;
        }
    }
}