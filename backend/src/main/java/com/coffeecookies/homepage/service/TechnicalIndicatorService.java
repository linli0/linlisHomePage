package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.repository.GoldPriceRepository;
import com.coffeecookies.homepage.entity.GoldPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechnicalIndicatorService {

    private final GoldPriceRepository goldPriceRepository;

    /**
     * 获取历史价格数据
     * 
     * @param symbol 货币符号（目前只支持USD）
     * @param days 天数
     * @return 价格列表
     */
    public List<BigDecimal> getHistoricalPrices(String symbol, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<GoldPrice> prices = goldPriceRepository.findByRecordedAtAfterOrderByRecordedAtAsc(startTime);
        
        List<BigDecimal> priceList = new ArrayList<>();
        for (GoldPrice price : prices) {
            priceList.add(price.getPriceUsd());
        }
        
        return priceList;
    }

    /**
     * 计算简单移动平均线 (SMA)
     * 
     * @param prices 价格列表
     * @param period 周期
     * @return SMA值
     */
    public BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        if (prices == null || prices.isEmpty() || period <= 0 || prices.size() < period) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        int startIndex = Math.max(0, prices.size() - period);
        
        for (int i = startIndex; i < prices.size(); i++) {
            sum = sum.add(prices.get(i));
        }
        
        return sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }

    /**
     * 计算指数移动平均线 (EMA)
     * 
     * @param prices 价格列表
     * @param period 周期
     * @return EMA值
     */
    public BigDecimal calculateEMA(List<BigDecimal> prices, int period) {
        if (prices == null || prices.isEmpty() || period <= 0 || prices.size() < period) {
            return BigDecimal.ZERO;
        }
        
        // 首先计算SMA作为初始EMA
        BigDecimal sma = calculateSMA(prices.subList(0, period), period);
        BigDecimal multiplier = BigDecimal.valueOf(2.0).divide(BigDecimal.valueOf(period + 1), 8, RoundingMode.HALF_UP);
        
        BigDecimal ema = sma;
        for (int i = period; i < prices.size(); i++) {
            BigDecimal currentPrice = prices.get(i);
            ema = currentPrice.multiply(multiplier)
                    .add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
        }
        
        return ema.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 计算MACD指标
     * 
     * @param prices 价格列表
     * @return 包含macd、signal、histogram的Map
     */
    public Map<String, BigDecimal> calculateMACD(List<BigDecimal> prices) {
        Map<String, BigDecimal> result = new HashMap<>();
        
        if (prices == null || prices.isEmpty()) {
            result.put("macd", BigDecimal.ZERO);
            result.put("signal", BigDecimal.ZERO);
            result.put("histogram", BigDecimal.ZERO);
            return result;
        }
        
        // MACD通常使用12天和26天EMA
        BigDecimal ema12 = calculateEMA(prices, 12);
        BigDecimal ema26 = calculateEMA(prices, 26);
        BigDecimal macd = ema12.subtract(ema26);
        
        // Signal线通常是9天EMA of MACD
        List<BigDecimal> macdValues = new ArrayList<>();
        // 为了简化，我们假设有一个足够长的历史来计算signal
        // 在实际应用中，可能需要存储历史MACD值
        macdValues.add(macd);
        BigDecimal signal = macd; // 简化处理，实际应基于历史MACD值计算
        
        BigDecimal histogram = macd.subtract(signal);
        
        result.put("macd", macd.setScale(4, RoundingMode.HALF_UP));
        result.put("signal", signal.setScale(4, RoundingMode.HALF_UP));
        result.put("histogram", histogram.setScale(4, RoundingMode.HALF_UP));
        
        return result;
    }

    /**
     * 计算相对强弱指数 (RSI)
     * 
     * @param prices 价格列表
     * @param period 周期（通常为14）
     * @return RSI值（0-100）
     */
    public BigDecimal calculateRSI(List<BigDecimal> prices, int period) {
        if (prices == null || prices.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }
        
        // 只取最后period个值
        int startIndex = Math.max(0, gains.size() - period);
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;
        
        for (int i = startIndex; i < gains.size(); i++) {
            avgGain = avgGain.add(gains.get(i));
            avgLoss = avgLoss.add(losses.get(i));
        }
        
        if (gains.size() > 0) {
            avgGain = avgGain.divide(BigDecimal.valueOf(Math.min(period, gains.size())), 8, RoundingMode.HALF_UP);
        }
        if (losses.size() > 0) {
            avgLoss = avgLoss.divide(BigDecimal.valueOf(Math.min(period, losses.size())), 8, RoundingMode.HALF_UP);
        }
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100");
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 8, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100).subtract(
                BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 8, RoundingMode.HALF_UP)
        );
        
        return rsi.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算布林带 (Bollinger Bands)
     * 
     * @param prices 价格列表
     * @param period 周期（通常为20）
     * @param stdDevMultiplier 标准差倍数（通常为2）
     * @return 包含upper、middle、lower的Map
     */
    public Map<String, BigDecimal> calculateBollingerBands(List<BigDecimal> prices, int period, int stdDevMultiplier) {
        Map<String, BigDecimal> result = new HashMap<>();
        
        if (prices == null || prices.size() < period) {
            result.put("upper", BigDecimal.ZERO);
            result.put("middle", BigDecimal.ZERO);
            result.put("lower", BigDecimal.ZERO);
            return result;
        }
        
        // 只取最后period个值
        int startIndex = Math.max(0, prices.size() - period);
        List<BigDecimal> recentPrices = prices.subList(startIndex, prices.size());
        
        // 计算中轨（SMA）
        BigDecimal middle = calculateSMA(recentPrices, period);
        
        // 计算标准差
        BigDecimal sumSquaredDifferences = BigDecimal.ZERO;
        for (BigDecimal price : recentPrices) {
            BigDecimal difference = price.subtract(middle);
            sumSquaredDifferences = sumSquaredDifferences.add(difference.multiply(difference));
        }
        
        BigDecimal variance = sumSquaredDifferences.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        
        // 计算上下轨
        BigDecimal upper = middle.add(stdDev.multiply(BigDecimal.valueOf(stdDevMultiplier)));
        BigDecimal lower = middle.subtract(stdDev.multiply(BigDecimal.valueOf(stdDevMultiplier)));
        
        result.put("upper", upper.setScale(4, RoundingMode.HALF_UP));
        result.put("middle", middle.setScale(4, RoundingMode.HALF_UP));
        result.put("lower", lower.setScale(4, RoundingMode.HALF_UP));
        
        return result;
    }

    /**
     * 计算平均真实波幅 (ATR)
     * 
     * @param highs 最高价列表
     * @param lows 最低价列表
     * @param closes 收盘价列表
     * @param period 周期（通常为14）
     * @return ATR值
     */
    public BigDecimal calculateATR(List<BigDecimal> highs, List<BigDecimal> lows, List<BigDecimal> closes, int period) {
        if (highs == null || lows == null || closes == null || 
            highs.size() != lows.size() || highs.size() != closes.size() || 
            highs.size() < period + 1) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> trValues = new ArrayList<>();
        
        for (int i = 1; i < highs.size(); i++) {
            BigDecimal high = highs.get(i);
            BigDecimal low = lows.get(i);
            BigDecimal prevClose = closes.get(i - 1);
            BigDecimal currentClose = closes.get(i);
            
            // 计算真实波幅(TR)
            BigDecimal tr1 = high.subtract(low);
            BigDecimal tr2 = high.subtract(prevClose).abs();
            BigDecimal tr3 = low.subtract(prevClose).abs();
            
            BigDecimal tr = tr1.max(tr2).max(tr3);
            trValues.add(tr);
        }
        
        // 计算ATR（TR的SMA）
        if (trValues.size() < period) {
            return BigDecimal.ZERO;
        }
        
        int startIndex = Math.max(0, trValues.size() - period);
        List<BigDecimal> recentTrValues = trValues.subList(startIndex, trValues.size());
        BigDecimal atr = calculateSMA(recentTrValues, period);
        
        return atr.setScale(4, RoundingMode.HALF_UP);
    }
}