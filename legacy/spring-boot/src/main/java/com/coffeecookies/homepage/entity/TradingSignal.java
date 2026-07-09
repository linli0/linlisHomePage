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
@Table(name = "trading_signals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TradingSignal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long strategyId;
    
    @Column(nullable = false)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalType signalType;
    
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal price;
    
    @Column(precision = 15, scale = 4)
    private BigDecimal targetPrice;
    
    @Column(precision = 15, scale = 4)
    private BigDecimal stopLoss;
    
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;
    
    @Column(nullable = false)
    private String reason;
    
    @Column(nullable = false)
    private boolean executed = false;
    
    @Column
    private LocalDateTime executedAt;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    public enum SignalType {
        BUY,
        SELL,
        HOLD
    }
}