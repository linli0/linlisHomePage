package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.MarketAnalysisDTO;
import com.coffeecookies.homepage.dto.TradingSignalDTO;
import com.coffeecookies.homepage.entity.TradingSignal;
import com.coffeecookies.homepage.entity.TradingStrategy;
import com.coffeecookies.homepage.repository.GoldPriceRepository;
import com.coffeecookies.homepage.repository.TradingSignalRepository;
import com.coffeecookies.homepage.repository.TradingStrategyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SignalGenerationService {

    private final TradingStrategyRepository tradingStrategyRepository;
    private final TradingSignalRepository tradingSignalRepository;
    private final GoldPriceRepository goldPriceRepository;
    private final TechnicalIndicatorService technicalIndicatorService;

    /**
     * Generates trading signals for a specific strategy and saves them to the database
     * 
     * @param strategyId the ID of the trading strategy
     * @return the generated trading signal
     */
    public TradingSignal generateSignals(Long strategyId) {
        log.info("Generating signals for strategy ID: {}", strategyId);
        
        // Retrieve the trading strategy
        TradingStrategy strategy = tradingStrategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Trading strategy not found with ID: " + strategyId));
        
        // Analyze market conditions for the strategy's symbol
        MarketAnalysisDTO marketAnalysis = analyzeMarketConditions(strategy.getSymbol());
        
        // Determine signal type based on market conditions
        TradingSignal.SignalType signalType;
        if (isBuySignal(marketAnalysis.getIndicators())) {
            signalType = TradingSignal.SignalType.BUY;
        } else if (isSellSignal(marketAnalysis.getIndicators())) {
            signalType = TradingSignal.SignalType.SELL;
        } else {
            signalType = TradingSignal.SignalType.HOLD;
        }
        
        // Calculate confidence score
        BigDecimal confidenceScore = calculateConfidenceScore(marketAnalysis.getIndicators());
        
        // Create and save the trading signal
        TradingSignal signal = new TradingSignal();
        signal.setStrategyId(strategy.getId());
        signal.setSymbol(strategy.getSymbol());
        signal.setSignalType(signalType);
        signal.setConfidence(confidenceScore);
        signal.setCreatedAt(LocalDateTime.now());
        signal.setExecuted(false);
        signal.setPrice(marketAnalysis.getCurrentPrice());
        signal.setReason(marketAnalysis.getIndicators().toString());
        
        TradingSignal savedSignal = tradingSignalRepository.save(signal);
        log.info("Generated {} signal for {} with confidence score: {}", 
                signalType, strategy.getSymbol(), confidenceScore);
        
        return savedSignal;
    }

    /**
     * Analyzes market conditions for a given symbol and returns market analysis data
     * 
     * @param symbol the trading symbol (e.g., "XAU/USD")
     * @return MarketAnalysisDTO containing current price and technical indicators
     */
    public MarketAnalysisDTO analyzeMarketConditions(String symbol) {
        log.debug("Analyzing market conditions for symbol: {}", symbol);
        
        // Get current price from gold price repository
        BigDecimal currentPrice = goldPriceRepository.findTopByOrderByRecordedAtDesc()
                .map(gp -> gp.getPriceUsd())
                .orElse(BigDecimal.valueOf(2000.00)); // Default fallback price
        
        // Get technical indicators from TechnicalIndicatorService
        Map<String, Object> indicators = buildIndicators(symbol, currentPrice);
        
        return MarketAnalysisDTO.builder()
                .symbol(symbol)
                .currentPrice(currentPrice)
                .indicators(indicators)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private Map<String, Object> buildIndicators(String symbol, BigDecimal currentPrice) {
        Map<String, Object> indicators = new HashMap<>();
        indicators.put("price", currentPrice);

        List<BigDecimal> prices = technicalIndicatorService.getHistoricalPrices(symbol, 60);
        if (prices.isEmpty()) {
            indicators.put("rsi", BigDecimal.ZERO);
            indicators.put("upperBand", currentPrice);
            indicators.put("lowerBand", currentPrice);
            indicators.put("macd", BigDecimal.ZERO);
            indicators.put("signalLine", BigDecimal.ZERO);
            indicators.put("volume", BigDecimal.ZERO);
            return indicators;
        }

        BigDecimal rsi = technicalIndicatorService.calculateRSI(prices, 14);
        Map<String, BigDecimal> bb = technicalIndicatorService.calculateBollingerBands(prices, 20, 2);
        Map<String, BigDecimal> macd = technicalIndicatorService.calculateMACD(prices);

        indicators.put("rsi", rsi);
        indicators.put("upperBand", bb.getOrDefault("upper", currentPrice));
        indicators.put("lowerBand", bb.getOrDefault("lower", currentPrice));
        indicators.put("macd", macd.getOrDefault("macd", BigDecimal.ZERO));
        indicators.put("signalLine", macd.getOrDefault("signal", BigDecimal.ZERO));
        indicators.put("volume", BigDecimal.ZERO);
        return indicators;
    }

    /**
     * Calculates a confidence score based on technical indicators
     * 
     * @param indicators map of technical indicators
     * @return confidence score as BigDecimal between 0-100
     */
    public BigDecimal calculateConfidenceScore(Map<String, Object> indicators) {
        BigDecimal totalScore = BigDecimal.ZERO;
        int factorCount = 0;
        
        // RSI factor (30 points max)
        if (indicators.containsKey("rsi")) {
            BigDecimal rsi = new BigDecimal(indicators.get("rsi").toString());
            BigDecimal rsiScore;
            
            if (rsi.compareTo(BigDecimal.valueOf(30)) < 0) {
                // Strong buy signal when RSI < 30
                rsiScore = BigDecimal.valueOf(30).subtract(rsi).divide(BigDecimal.valueOf(0.7), 2, RoundingMode.HALF_UP);
                rsiScore = rsiScore.min(BigDecimal.valueOf(30));
            } else if (rsi.compareTo(BigDecimal.valueOf(70)) > 0) {
                // Strong sell signal when RSI > 70
                rsiScore = rsi.subtract(BigDecimal.valueOf(70)).divide(BigDecimal.valueOf(0.3), 2, RoundingMode.HALF_UP);
                rsiScore = rsiScore.min(BigDecimal.valueOf(30));
            } else {
                // Neutral RSI gives lower score
                rsiScore = BigDecimal.valueOf(10);
            }
            
            totalScore = totalScore.add(rsiScore);
            factorCount++;
        }
        
        // Bollinger Bands factor (40 points max)
        if (indicators.containsKey("price") && 
            indicators.containsKey("upperBand") && 
            indicators.containsKey("lowerBand")) {
            
            BigDecimal price = new BigDecimal(indicators.get("price").toString());
            BigDecimal upperBand = new BigDecimal(indicators.get("upperBand").toString());
            BigDecimal lowerBand = new BigDecimal(indicators.get("lowerBand").toString());
            
            BigDecimal bbScore;
            boolean isBuyCondition = price.compareTo(lowerBand) < 0;
            boolean isSellCondition = price.compareTo(upperBand) > 0;
            
            if (isBuyCondition || isSellCondition) {
                // Strong signal when price crosses bands
                bbScore = BigDecimal.valueOf(40);
            } else {
                // Calculate proximity to bands for partial score
                BigDecimal distanceToLower = price.subtract(lowerBand).abs();
                BigDecimal distanceToUpper = upperBand.subtract(price).abs();
                BigDecimal bandWidth = upperBand.subtract(lowerBand);
                
                if (bandWidth.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal proximityScore = distanceToLower.min(distanceToUpper)
                            .divide(bandWidth, 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(20));
                    bbScore = BigDecimal.valueOf(20).subtract(proximityScore);
                } else {
                    bbScore = BigDecimal.valueOf(10);
                }
            }
            
            totalScore = totalScore.add(bbScore);
            factorCount++;
        }
        
        // Additional indicators can contribute to score (30 points max)
        if (indicators.containsKey("macd") && indicators.containsKey("signalLine")) {
            BigDecimal macd = new BigDecimal(indicators.get("macd").toString());
            BigDecimal signalLine = new BigDecimal(indicators.get("signalLine").toString());
            BigDecimal macdDiff = macd.subtract(signalLine).abs();
            
            // MACD crossover adds confidence
            if (macdDiff.compareTo(BigDecimal.valueOf(0.5)) > 0) {
                totalScore = totalScore.add(BigDecimal.valueOf(15));
                factorCount++;
            }
        }
        
        if (indicators.containsKey("volume")) {
            // High volume adds confidence
            BigDecimal volume = new BigDecimal(indicators.get("volume").toString());
            if (volume.compareTo(BigDecimal.valueOf(1000)) > 0) {
                totalScore = totalScore.add(BigDecimal.valueOf(15));
                factorCount++;
            }
        }
        
        // Normalize score to 0-100 range
        if (factorCount > 0) {
            BigDecimal maxPossibleScore = BigDecimal.valueOf(factorCount * 30);
            if (maxPossibleScore.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal normalizedScore = totalScore.multiply(BigDecimal.valueOf(100))
                        .divide(maxPossibleScore, 2, RoundingMode.HALF_UP);
                return normalizedScore.min(BigDecimal.valueOf(100));
            }
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Returns a list of active (unexecuted) trading signals
     * 
     * @return list of TradingSignalDTO objects
     */
    @Transactional(readOnly = true)
    public List<TradingSignalDTO> getActiveSignals() {
        log.debug("Retrieving active trading signals");
        List<TradingSignal> activeSignals = tradingSignalRepository.findByExecuted(false).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        
        return activeSignals.stream()
                .map(signal -> TradingSignalDTO.builder()
                        .id(signal.getId())
                        .strategyId(signal.getStrategyId())
                        .symbol(signal.getSymbol())
                        .signalType(signal.getSignalType().name())
                        .confidence(signal.getConfidence())
                        .createdAt(signal.getCreatedAt())
                        .price(signal.getPrice())
                        .executed(signal.isExecuted())
                        .reason(signal.getReason())
                        .targetPrice(signal.getTargetPrice())
                        .stopLoss(signal.getStopLoss())
                        .executedAt(signal.getExecutedAt())
                        .build())
                .toList();
    }

    /**
     * Determines if conditions indicate a BUY signal
     * BUY signal: when RSI < 30 AND price crosses below lower Bollinger Band
     */
    private boolean isBuySignal(Map<String, Object> indicators) {
        if (!indicators.containsKey("rsi") || 
            !indicators.containsKey("price") || 
            !indicators.containsKey("lowerBand")) {
            return false;
        }
        
        BigDecimal rsi = new BigDecimal(indicators.get("rsi").toString());
        BigDecimal price = new BigDecimal(indicators.get("price").toString());
        BigDecimal lowerBand = new BigDecimal(indicators.get("lowerBand").toString());
        
        return rsi.compareTo(BigDecimal.valueOf(30)) < 0 && 
               price.compareTo(lowerBand) < 0;
    }

    /**
     * Determines if conditions indicate a SELL signal
     * SELL signal: when RSI > 70 AND price crosses above upper Bollinger Band
     */
    private boolean isSellSignal(Map<String, Object> indicators) {
        if (!indicators.containsKey("rsi") || 
            !indicators.containsKey("price") || 
            !indicators.containsKey("upperBand")) {
            return false;
        }
        
        BigDecimal rsi = new BigDecimal(indicators.get("rsi").toString());
        BigDecimal price = new BigDecimal(indicators.get("price").toString());
        BigDecimal upperBand = new BigDecimal(indicators.get("upperBand").toString());
        
        return rsi.compareTo(BigDecimal.valueOf(70)) > 0 && 
               price.compareTo(upperBand) > 0;
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void generateSignalsForActiveStrategies() {
        try {
            log.info("Starting scheduled signal generation for active strategies");
            
            // Query all ACTIVE strategies from TradingStrategyRepository
            List<TradingStrategy> activeStrategies = tradingStrategyRepository.findByStatus(TradingStrategy.StrategyStatus.ACTIVE);
            
            log.info("Found {} active strategies to process", activeStrategies.size());
            
            // For each strategy, call generateSignals(strategyId)
            for (TradingStrategy strategy : activeStrategies) {
                try {
                    TradingSignal signal = generateSignals(strategy.getId());
                    log.info("Generated {} signal for strategy '{}' with confidence score: {}", 
                            signal.getSignalType(), strategy.getName(), signal.getConfidence());
                } catch (Exception e) {
                    log.error("Failed to generate signals for strategy ID {}: {}", strategy.getId(), e.getMessage(), e);
                }
            }
            
            log.info("Completed scheduled signal generation for {} active strategies", activeStrategies.size());
        } catch (Exception e) {
            log.error("Failed to generate signals for active strategies: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void cleanupOldSignals() {
        try {
            log.info("Starting scheduled cleanup of old signals");
            
            // Delete signals older than 30 days
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            int deletedCount = tradingSignalRepository.deleteByCreatedAtBefore(thirtyDaysAgo);
            
            log.info("Completed scheduled cleanup of old signals - deleted {} signals older than 30 days", deletedCount);
        } catch (Exception e) {
            log.error("Failed to cleanup old signals: {}", e.getMessage(), e);
        }
    }
}
