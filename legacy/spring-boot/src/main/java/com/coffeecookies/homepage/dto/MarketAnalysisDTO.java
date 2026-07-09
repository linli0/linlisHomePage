package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnalysisDTO {

    private String symbol;
    private BigDecimal currentPrice;
    private Map<String, Object> indicators;
    private LocalDateTime timestamp;
}
