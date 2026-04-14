package com.coffeecookies.homepage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "backtest_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BacktestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long strategyId;
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @Column(nullable = false)
    private LocalDateTime endDate;
    
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal totalReturn;
    
    @Column(precision = 8, scale = 4)
    private BigDecimal sharpeRatio;
    
    @Column(precision = 8, scale = 4)
    private BigDecimal maxDrawdown;
    
    @Column(precision = 5, scale = 4)
    private BigDecimal winRate;
    
    @Column(nullable = false)
    private Integer totalTrades;
    
    @Column(nullable = false)
    private Integer profitTrades;
    
    @Column(nullable = false)
    private Integer lossTrades;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal averageProfit;
    
    @Column(precision = 10, scale = 4)
    private BigDecimal averageLoss;
    
    @Column(columnDefinition = "TEXT")
    private String parameters;
    
    @CreatedDate
    private LocalDateTime createdAt;
}