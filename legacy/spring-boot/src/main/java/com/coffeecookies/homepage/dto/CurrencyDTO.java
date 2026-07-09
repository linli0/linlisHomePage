package com.coffeecookies.homepage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDTO {
    
    private String code;
    private String name;
    private String symbol;
    private String flag;
    private BigDecimal rate;
}
