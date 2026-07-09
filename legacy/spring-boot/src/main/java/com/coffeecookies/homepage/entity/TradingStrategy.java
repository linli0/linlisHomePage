package com.coffeecookies.homepage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trading_strategies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TradingStrategy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StrategyStatus status;
    
    @Column
    private String parameters;
    
    @Column(nullable = false)
    private String symbol;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal totalReturn;
    
    @Column(precision = 8, scale = 4)
    private BigDecimal sharpeRatio;
    
    @Column(precision = 8, scale = 4)
    private BigDecimal maxDrawdown;
    
    @Column(precision = 5, scale = 4)
    private BigDecimal winRate;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum StrategyType {
        TREND,
        MEAN_REVERSION,
        ARBITRAGE,
        MOMENTUM
    }
    
    public enum StrategyStatus {
        ACTIVE,
        INACTIVE,
        PAUSED
    }
}