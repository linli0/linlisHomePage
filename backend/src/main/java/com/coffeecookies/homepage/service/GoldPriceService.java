package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.CurrencyDTO;
import com.coffeecookies.homepage.dto.GoldPriceDTO;
import com.coffeecookies.homepage.entity.ExchangeRate;
import com.coffeecookies.homepage.entity.GoldPrice;
import com.coffeecookies.homepage.repository.ExchangeRateRepository;
import com.coffeecookies.homepage.repository.GoldPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoldPriceService {

    private final GoldPriceRepository goldPriceRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final WebClient webClient = WebClient.builder().build();

    private static final Map<String, CurrencyInfo> CURRENCIES = Map.of(
            "USD", new CurrencyInfo("USD", "美元", "$", "🇺🇸"),
            "CNY", new CurrencyInfo("CNY", "人民币", "¥", "🇨🇳"),
            "EUR", new CurrencyInfo("EUR", "欧元", "€", "🇪🇺"),
            "GBP", new CurrencyInfo("GBP", "英镑", "£", "🇬🇧")
    );

    @Transactional(readOnly = true)
    public GoldPriceDTO getCurrentPrice(String currency) {
        GoldPrice latest = goldPriceRepository.findTopByOrderByRecordedAtDesc()
                .orElseGet(this::createInitialPrice);
        
        return convertToDTO(latest, currency);
    }

    @Transactional(readOnly = true)
    public List<GoldPriceDTO.PricePoint> getPriceHistory(String currency, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<GoldPrice> prices = goldPriceRepository.findByRecordedAtAfterOrderByRecordedAtAsc(startTime);
        
        if (prices.isEmpty()) {
            prices = generateMockHistory(days);
        }
        
        BigDecimal rate = getExchangeRate("USD", currency);
        
        List<GoldPriceDTO.PricePoint> points = new ArrayList<>();
        for (GoldPrice price : prices) {
            points.add(GoldPriceDTO.PricePoint.builder()
                    .date(price.getRecordedAt().toLocalDate().toString())
                    .price(price.getPriceUsd().multiply(rate).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }
        
        return points;
    }

    @Transactional(readOnly = true)
    public List<CurrencyDTO> getSupportedCurrencies() {
        List<CurrencyDTO> currencies = new ArrayList<>();
        
        for (Map.Entry<String, CurrencyInfo> entry : CURRENCIES.entrySet()) {
            CurrencyInfo info = entry.getValue();
            BigDecimal rate = getExchangeRate("USD", entry.getKey());
            
            currencies.add(CurrencyDTO.builder()
                    .code(entry.getKey())
                    .name(info.name())
                    .symbol(info.symbol())
                    .flag(info.flag())
                    .rate(rate)
                    .build());
        }
        
        return currencies;
    }

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void updateGoldPrice() {
        try {
            BigDecimal newPrice = fetchGoldPriceFromAPI();
            
            GoldPrice lastPrice = goldPriceRepository.findTopByOrderByRecordedAtDesc().orElse(null);
            BigDecimal changeAmount = BigDecimal.ZERO;
            BigDecimal changePercent = BigDecimal.ZERO;
            
            if (lastPrice != null) {
                changeAmount = newPrice.subtract(lastPrice.getPriceUsd());
                changePercent = changeAmount.multiply(BigDecimal.valueOf(100))
                        .divide(lastPrice.getPriceUsd(), 4, RoundingMode.HALF_UP);
            }
            
            GoldPrice goldPrice = new GoldPrice();
            goldPrice.setPriceUsd(newPrice);
            goldPrice.setChangeAmount(changeAmount);
            goldPrice.setChangePercent(changePercent);
            goldPrice.setCurrency("USD");
            
            goldPriceRepository.save(goldPrice);
            log.info("Gold price updated: {} USD", newPrice);
            
            // Update exchange rates
            updateExchangeRates();
            
        } catch (Exception e) {
            log.error("Failed to update gold price", e);
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void updateExchangeRates() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("https://api.exchangerate-api.com/v4/latest/USD")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (response != null && response.containsKey("rates")) {
                Map<String, Number> rates = (Map<String, Number>) response.get("rates");
                
                for (String currency : CURRENCIES.keySet()) {
                    if (rates.containsKey(currency)) {
                        BigDecimal rate = BigDecimal.valueOf(rates.get(currency).doubleValue());
                        
                        ExchangeRate exchangeRate = exchangeRateRepository
                                .findByFromCurrencyAndToCurrency("USD", currency)
                                .orElse(new ExchangeRate());
                        
                        exchangeRate.setFromCurrency("USD");
                        exchangeRate.setToCurrency(currency);
                        exchangeRate.setRate(rate);
                        
                        exchangeRateRepository.save(exchangeRate);
                    }
                }
                log.info("Exchange rates updated successfully");
            }
        } catch (Exception e) {
            log.error("Failed to update exchange rates", e);
        }
    }

    private BigDecimal fetchGoldPriceFromAPI() {
        // In production, use a real gold price API
        // For demo, simulate price with small random variation around $2000-2100
        GoldPrice lastPrice = goldPriceRepository.findTopByOrderByRecordedAtDesc().orElse(null);
        
        BigDecimal basePrice = lastPrice != null ? lastPrice.getPriceUsd() : new BigDecimal("2050.00");
        
        // Random variation between -5 and +5
        double variation = (Math.random() - 0.5) * 10;
        return basePrice.add(BigDecimal.valueOf(variation)).setScale(2, RoundingMode.HALF_UP);
    }

    private GoldPrice createInitialPrice() {
        GoldPrice price = new GoldPrice();
        price.setPriceUsd(new BigDecimal("2050.00"));
        price.setChangeAmount(BigDecimal.ZERO);
        price.setChangePercent(BigDecimal.ZERO);
        price.setCurrency("USD");
        price.setRecordedAt(LocalDateTime.now());
        return goldPriceRepository.save(price);
    }

    private List<GoldPrice> generateMockHistory(int days) {
        List<GoldPrice> history = new ArrayList<>();
        BigDecimal basePrice = new BigDecimal("2000.00");
        
        for (int i = days; i >= 0; i--) {
            GoldPrice price = new GoldPrice();
            double variation = (Math.random() - 0.45) * 20; // Slight upward trend
            BigDecimal priceValue = basePrice.add(BigDecimal.valueOf(variation * (days - i) / days));
            
            price.setPriceUsd(priceValue.setScale(2, RoundingMode.HALF_UP));
            price.setChangeAmount(BigDecimal.ZERO);
            price.setChangePercent(BigDecimal.ZERO);
            price.setCurrency("USD");
            price.setRecordedAt(LocalDateTime.now().minusDays(i));
            
            history.add(price);
        }
        
        return goldPriceRepository.saveAll(history);
    }

    private GoldPriceDTO convertToDTO(GoldPrice goldPrice, String currency) {
        BigDecimal rate = getExchangeRate("USD", currency);
        BigDecimal convertedPrice = goldPrice.getPriceUsd().multiply(rate);
        BigDecimal convertedChange = goldPrice.getChangeAmount().multiply(rate);
        
        CurrencyInfo info = CURRENCIES.get(currency);
        
        // Calculate statistics from last 30 days
        List<GoldPrice> history = goldPriceRepository.findByRecordedAtAfterOrderByRecordedAtAsc(
                LocalDateTime.now().minusDays(30));
        
        BigDecimal high = convertedPrice;
        BigDecimal low = convertedPrice;
        BigDecimal avg = convertedPrice;
        BigDecimal volatility = BigDecimal.ZERO;
        
        if (!history.isEmpty()) {
            List<BigDecimal> prices = history.stream()
                    .map(p -> p.getPriceUsd().multiply(rate))
                    .toList();
            
            high = prices.stream().max(BigDecimal::compareTo).orElse(convertedPrice);
            low = prices.stream().min(BigDecimal::compareTo).orElse(convertedPrice);
            avg = prices.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
            
            // Calculate volatility (standard deviation)
            BigDecimal mean = avg;
            double variance = prices.stream()
                    .mapToDouble(p -> Math.pow(p.subtract(mean).doubleValue(), 2))
                    .average().orElse(0.0);
            volatility = BigDecimal.valueOf(Math.sqrt(variance) / mean.doubleValue() * 100)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        
        return GoldPriceDTO.builder()
                .price(convertedPrice.setScale(2, RoundingMode.HALF_UP))
                .changeAmount(convertedChange.setScale(2, RoundingMode.HALF_UP))
                .changePercent(goldPrice.getChangePercent())
                .currency(currency)
                .symbol(info.symbol())
                .timestamp(goldPrice.getRecordedAt())
                .high(high.setScale(2, RoundingMode.HALF_UP))
                .low(low.setScale(2, RoundingMode.HALF_UP))
                .average(avg.setScale(2, RoundingMode.HALF_UP))
                .volatility(volatility)
                .build();
    }

    private BigDecimal getExchangeRate(String from, String to) {
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }
        
        return exchangeRateRepository.findByFromCurrencyAndToCurrency(from, to)
                .map(ExchangeRate::getRate)
                .orElseGet(() -> {
                    // Fallback rates
                    return switch (to) {
                        case "USD" -> BigDecimal.ONE;
                        case "CNY" -> new BigDecimal("7.20");
                        case "EUR" -> new BigDecimal("0.92");
                        case "GBP" -> new BigDecimal("0.79");
                        default -> BigDecimal.ONE;
                    };
                });
    }

    private record CurrencyInfo(String code, String name, String symbol, String flag) {}
}
