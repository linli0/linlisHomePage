package com.coffeecookies.homepage.repository;

import com.coffeecookies.homepage.entity.TradingSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradingSignalRepository extends JpaRepository<TradingSignal, Long> {
    
    List<TradingSignal> findByStrategyId(Long strategyId);
    
    List<TradingSignal> findBySymbol(String symbol);
    
    List<TradingSignal> findBySignalType(TradingSignal.SignalType signalType);
    
    List<TradingSignal> findByExecuted(boolean executed);
    
    @Query("SELECT s FROM TradingSignal s WHERE s.createdAt >= :startTime ORDER BY s.createdAt DESC")
    List<TradingSignal> findByCreatedAtAfter(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT s FROM TradingSignal s WHERE s.createdAt < :cutoffTime")
    List<TradingSignal> findByCreatedAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM TradingSignal s WHERE s.createdAt < :cutoffTime")
    int deleteByCreatedAtBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    List<TradingSignal> findTop100ByOrderByCreatedAtDesc();
}