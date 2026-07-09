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
public class GoldPriceDTO {
    
    private BigDecimal price;
    private BigDecimal changeAmount;
    private BigDecimal changePercent;
    private String currency;
    private String symbol;
    private LocalDateTime timestamp;
    
    // Statistics
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal average;
    private BigDecimal volatility;
    
    // Chart data
    private List<PricePoint> history;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePoint {
        private String date;
        private BigDecimal price;
    }
}
