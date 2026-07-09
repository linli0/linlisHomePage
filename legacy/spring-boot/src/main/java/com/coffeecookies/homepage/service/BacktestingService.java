package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.BacktestResultDTO;
import com.coffeecookies.homepage.entity.BacktestResult;
import com.coffeecookies.homepage.entity.GoldPrice;
import com.coffeecookies.homepage.entity.TradingSignal;
import com.coffeecookies.homepage.entity.TradingStrategy;
import com.coffeecookies.homepage.repository.BacktestResultRepository;
import com.coffeecookies.homepage.repository.GoldPriceRepository;
import com.coffeecookies.homepage.repository.TradingStrategyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BacktestingService {

    private final GoldPriceRepository goldPriceRepository;
    private final TradingStrategyRepository tradingStrategyRepository;
    private final BacktestResultRepository backtestResultRepository;
    private final TechnicalIndicatorService technicalIndicatorService;
    private final SignalGenerationService signalGenerationService;

    /**
     * Runs a backtest for the specified strategy with given parameters
     * 
     * @param strategyId The ID of the trading strategy to backtest
     * @param parameters The parameters for the backtest
     * @return The saved backtest result
     */
    public BacktestResult runBacktest(Long strategyId, String parameters) {
        log.info("Starting backtest for strategy ID: {} with parameters: {}", strategyId, parameters);
        
        // Retrieve the trading strategy
        TradingStrategy strategy = tradingStrategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Trading strategy not found with ID: " + strategyId));
        
        // Get historical gold price data (last 365 days for comprehensive backtesting)
        LocalDateTime startDate = LocalDateTime.now().minusDays(365);
        List<GoldPrice> historicalPrices = goldPriceRepository.findByRecordedAtAfterOrderByRecordedAtAsc(startDate);
        
        if (historicalPrices.isEmpty()) {
            throw new IllegalStateException("No historical price data available for backtesting");
        }
        
        // Generate trading signals based on the strategy
        TradingSignal generatedSignal = signalGenerationService.generateSignals(strategyId);
        List<TradeResult> trades = mapSignalToTrades(generatedSignal, historicalPrices);
        
        // Calculate performance metrics
        PerformanceMetrics metrics = calculatePerformanceMetrics(trades);
        
        // Create and save backtest result
        BacktestResult result = new BacktestResult();
        result.setStrategyId(strategy.getId());
        result.setParameters(parameters);
        result.setTotalReturn(metrics.getTotalReturn());
        result.setSharpeRatio(metrics.getSharpeRatio());
        result.setMaxDrawdown(metrics.getMaxDrawdown());
        result.setWinRate(metrics.getWinRate());
        int totalTrades = trades.size();
        int profitTrades = (int) trades.stream()
                .filter(trade -> trade.getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        int lossTrades = totalTrades - profitTrades;
        result.setTotalTrades(totalTrades);
        result.setProfitTrades(profitTrades);
        result.setLossTrades(lossTrades);
        result.setAverageProfit(calculateAverageProfit(trades));
        result.setAverageLoss(calculateAverageLoss(trades));
        result.setStartDate(historicalPrices.get(0).getRecordedAt());
        result.setEndDate(historicalPrices.get(historicalPrices.size() - 1).getRecordedAt());
        result.setCreatedAt(LocalDateTime.now());
        
        BacktestResult savedResult = backtestResultRepository.save(result);
        log.info("Backtest completed successfully for strategy ID: {}, result ID: {}", strategyId, savedResult.getId());
        
        return savedResult;
    }

    /**
     * Calculates performance metrics from a list of trade results
     * 
     * @param trades List of trade results
     * @return PerformanceMetrics object containing calculated metrics
     */
    public PerformanceMetrics calculatePerformanceMetrics(List<TradeResult> trades) {
        if (trades.isEmpty()) {
            return new PerformanceMetrics(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
            );
        }
        
        // Calculate total return
        BigDecimal initialValue = BigDecimal.valueOf(10000); // Starting with $10,000
        BigDecimal finalValue = calculateFinalPortfolioValue(initialValue, trades);
        BigDecimal totalReturn = finalValue.subtract(initialValue)
                .divide(initialValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        // Calculate win rate
        long profitTrades = trades.stream()
                .filter(trade -> trade.getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        BigDecimal winRate = BigDecimal.valueOf(profitTrades)
                .divide(BigDecimal.valueOf(trades.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        // Calculate max drawdown
        BigDecimal maxDrawdown = calculateMaxDrawdown(initialValue, trades);
        
        // Calculate Sharpe ratio (annualized)
        BigDecimal sharpeRatio = calculateSharpeRatio(trades);
        
        return new PerformanceMetrics(totalReturn, sharpeRatio, maxDrawdown, winRate);
    }

    /**
     * Gets the backtest history for a specific strategy
     * 
     * @param strategyId The ID of the trading strategy
     * @return List of BacktestResultDTO objects
     */
    @Transactional(readOnly = true)
    public List<BacktestResultDTO> getBacktestHistory(Long strategyId) {
        List<BacktestResult> results = backtestResultRepository.findByStrategyIdOrderByCreatedAtDesc(strategyId);
        
        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the final portfolio value after executing all trades
     */
    private BigDecimal calculateFinalPortfolioValue(BigDecimal initialValue, List<TradeResult> trades) {
        BigDecimal currentValue = initialValue;
        
        for (TradeResult trade : trades) {
            // Apply profit/loss to current value
            currentValue = currentValue.add(trade.getProfitLoss());
        }
        
        return currentValue;
    }

    /**
     * Calculates the maximum drawdown as a percentage
     */
    private BigDecimal calculateMaxDrawdown(BigDecimal initialValue, List<TradeResult> trades) {
        BigDecimal peak = initialValue;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal currentValue = initialValue;
        
        for (TradeResult trade : trades) {
            currentValue = currentValue.add(trade.getProfitLoss());
            
            // Update peak if current value is higher
            if (currentValue.compareTo(peak) > 0) {
                peak = currentValue;
            }
            
            // Calculate drawdown from peak
            BigDecimal drawdown = peak.subtract(currentValue)
                    .divide(peak, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            // Update max drawdown if current drawdown is larger
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }
        
        return maxDrawdown;
    }

    /**
     * Calculates the annualized Sharpe ratio
     */
    private BigDecimal calculateSharpeRatio(List<TradeResult> trades) {
        if (trades.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Calculate returns for each trade
        List<BigDecimal> returns = new ArrayList<>();
        for (TradeResult trade : trades) {
            // Assuming initial capital of $10,000 per trade for simplicity
            BigDecimal returnRate = trade.getProfitLoss().divide(BigDecimal.valueOf(10000), 6, RoundingMode.HALF_UP);
            returns.add(returnRate);
        }
        
        // Calculate average return
        BigDecimal sumReturns = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgReturn = sumReturns.divide(BigDecimal.valueOf(returns.size()), 6, RoundingMode.HALF_UP);
        
        // Calculate standard deviation
        BigDecimal sumSquaredDiffs = returns.stream()
                .map(r -> r.subtract(avgReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = sumSquaredDiffs.divide(BigDecimal.valueOf(returns.size() - 1), 6, RoundingMode.HALF_UP);
        double stdDev = Math.sqrt(variance.doubleValue());
        
        // Annualize (assuming ~252 trading days per year, and our trades span 365 days)
        // For simplicity, we'll annualize by multiplying by sqrt(252)
        if (stdDev == 0) {
            return BigDecimal.ZERO;
        }
        
        double annualizedAvgReturn = avgReturn.doubleValue() * 252;
        double annualizedStdDev = stdDev * Math.sqrt(252);
        double sharpeRatio = annualizedAvgReturn / annualizedStdDev;
        
        return BigDecimal.valueOf(sharpeRatio).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Converts BacktestResult entity to DTO
     */
    private BacktestResultDTO convertToDTO(BacktestResult result) {
        TradingStrategy strategy = tradingStrategyRepository.findById(result.getStrategyId()).orElse(null);
        return BacktestResultDTO.builder()
                .id(result.getId())
                .strategyId(result.getStrategyId())
                .strategyName(strategy != null ? strategy.getName() : null)
                .parameters(result.getParameters())
                .totalReturn(result.getTotalReturn())
                .sharpeRatio(result.getSharpeRatio())
                .maxDrawdown(result.getMaxDrawdown())
                .winRate(result.getWinRate())
                .totalTrades(result.getTotalTrades())
                .profitTrades(result.getProfitTrades())
                .lossTrades(result.getLossTrades())
                .averageProfit(result.getAverageProfit())
                .averageLoss(result.getAverageLoss())
                .startDate(result.getStartDate())
                .endDate(result.getEndDate())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private List<TradeResult> mapSignalToTrades(TradingSignal signal, List<GoldPrice> historicalPrices) {
        if (signal == null || historicalPrices.size() < 2) {
            return Collections.emptyList();
        }
        GoldPrice entry = historicalPrices.get(historicalPrices.size() - 2);
        GoldPrice exit = historicalPrices.get(historicalPrices.size() - 1);
        String direction = signal.getSignalType() == TradingSignal.SignalType.SELL ? "SHORT" : "LONG";
        TradeResult trade = new TradeResult(
                entry.getRecordedAt(),
                exit.getRecordedAt(),
                entry.getPriceUsd(),
                exit.getPriceUsd(),
                BigDecimal.ONE,
                direction
        );
        return List.of(trade);
    }

    private BigDecimal calculateAverageProfit(List<TradeResult> trades) {
        List<BigDecimal> profits = trades.stream()
                .map(TradeResult::getProfitLoss)
                .filter(value -> value.compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (profits.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = profits.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(profits.size()), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageLoss(List<TradeResult> trades) {
        List<BigDecimal> losses = trades.stream()
                .map(TradeResult::getProfitLoss)
                .filter(value -> value.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .toList();
        if (losses.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = losses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(losses.size()), 4, RoundingMode.HALF_UP);
    }

    /**
     * Inner class to hold performance metrics
     */
    public static class PerformanceMetrics {
        private final BigDecimal totalReturn;
        private final BigDecimal sharpeRatio;
        private final BigDecimal maxDrawdown;
        private final BigDecimal winRate;

        public PerformanceMetrics(BigDecimal totalReturn, BigDecimal sharpeRatio, 
                                BigDecimal maxDrawdown, BigDecimal winRate) {
            this.totalReturn = totalReturn;
            this.sharpeRatio = sharpeRatio;
            this.maxDrawdown = maxDrawdown;
            this.winRate = winRate;
        }

        public BigDecimal getTotalReturn() { return totalReturn; }
        public BigDecimal getSharpeRatio() { return sharpeRatio; }
        public BigDecimal getMaxDrawdown() { return maxDrawdown; }
        public BigDecimal getWinRate() { return winRate; }
    }

    /**
     * Inner class to represent a trade result
     */
    public static class TradeResult {
        private final LocalDateTime entryTime;
        private final LocalDateTime exitTime;
        private final BigDecimal entryPrice;
        private final BigDecimal exitPrice;
        private final BigDecimal quantity;
        private final BigDecimal profitLoss;
        private final String direction; // "LONG" or "SHORT"

        public TradeResult(LocalDateTime entryTime, LocalDateTime exitTime, 
                          BigDecimal entryPrice, BigDecimal exitPrice, 
                          BigDecimal quantity, String direction) {
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.entryPrice = entryPrice;
            this.exitPrice = exitPrice;
            this.quantity = quantity;
            this.direction = direction;
            
            // Calculate profit/loss
            if ("LONG".equals(direction)) {
                this.profitLoss = exitPrice.subtract(entryPrice).multiply(quantity);
            } else { // SHORT
                this.profitLoss = entryPrice.subtract(exitPrice).multiply(quantity);
            }
        }

        public LocalDateTime getEntryTime() { return entryTime; }
        public LocalDateTime getExitTime() { return exitTime; }
        public BigDecimal getEntryPrice() { return entryPrice; }
        public BigDecimal getExitPrice() { return exitPrice; }
        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal getProfitLoss() { return profitLoss; }
        public String getDirection() { return direction; }
    }
}
