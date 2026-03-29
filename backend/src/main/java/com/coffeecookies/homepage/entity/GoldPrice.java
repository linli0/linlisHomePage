package com.coffeecookies.homepage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gold_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GoldPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal priceUsd;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal changeAmount;
    
    @Column(precision = 5, scale = 4)
    private BigDecimal changePercent;
    
    @Column(nullable = false)
    private String currency = "USD";
    
    @CreatedDate
    private LocalDateTime recordedAt;
}
