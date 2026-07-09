package com.coffeecookies.homepage.repository;

import com.coffeecookies.homepage.entity.BacktestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BacktestResultRepository extends JpaRepository<BacktestResult, Long> {
    
    List<BacktestResult> findByStrategyId(Long strategyId);
    
    @Query("SELECT b FROM BacktestResult b WHERE b.strategyId = :strategyId ORDER BY b.createdAt DESC")
    List<BacktestResult> findByStrategyIdOrderByCreatedAtDesc(@Param("strategyId") Long strategyId);
    
    Optional<BacktestResult> findTopByStrategyIdOrderByCreatedAtDesc(Long strategyId);
}