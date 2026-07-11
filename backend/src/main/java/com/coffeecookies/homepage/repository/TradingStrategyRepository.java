package com.coffeecookies.homepage.repository;

import com.coffeecookies.homepage.entity.TradingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradingStrategyRepository extends JpaRepository<TradingStrategy, Long> {
    
    Optional<TradingStrategy> findByName(String name);
    
    List<TradingStrategy> findByStatus(TradingStrategy.StrategyStatus status);
    
    List<TradingStrategy> findByType(TradingStrategy.StrategyType type);
    
    List<TradingStrategy> findBySymbol(String symbol);
    
    @Query("SELECT s FROM TradingStrategy s WHERE s.status = :status ORDER BY s.totalReturn DESC")
    List<TradingStrategy> findTopPerformingStrategies(@Param("status") TradingStrategy.StrategyStatus status);
}