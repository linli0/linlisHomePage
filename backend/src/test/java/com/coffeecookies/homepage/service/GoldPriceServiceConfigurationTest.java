package com.coffeecookies.homepage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * GoldPriceService 配置验证测试
 * 测试 MetalpriceAPI 配置验证逻辑
 */
@ExtendWith(MockitoExtension.class)
class GoldPriceServiceConfigurationTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private GoldPriceService goldPriceService;

    @BeforeEach
    void setUp() {
        goldPriceService = new GoldPriceService(null, null, webClient);
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenMetalpriceApiDisabled() {
        // Given
        goldPriceService = new GoldPriceService(null, null, webClient);
        goldPriceService.setMetalpriceApiEnabled(false);
        goldPriceService.setMetalpriceApiKey("test-key");

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(
            "MetalpriceAPI is not enabled. Please set METALPRICE_ENABLED=true",
            exception.getMessage()
        );
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenApiKeyNotConfigured() {
        // Given
        goldPriceService = new GoldPriceService(null, null, webClient);
        goldPriceService.setMetalpriceApiEnabled(true);
        goldPriceService.setMetalpriceApiKey("");
        goldPriceService.setMetalpriceApiServer("us");

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(
            "MetalpriceAPI key is not configured. Please set METALPRICE_API_KEY environment variable",
            exception.getMessage()
        );
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenApiKeyIsNull() {
        // Given
        goldPriceService = new GoldPriceService(null, null, webClient);
        goldPriceService.setMetalpriceApiEnabled(true);
        goldPriceService.setMetalpriceApiKey(null);
        goldPriceService.setMetalpriceApiServer("us");

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(
            "MetalpriceAPI key is not configured. Please set METALPRICE_API_KEY environment variable",
            exception.getMessage()
        );
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenApiKeyContainsOnlySpaces() {
        // Given
        goldPriceService = new GoldPriceService(null, null, webClient);
        goldPriceService.setMetalpriceApiEnabled(true);
        goldPriceService.setMetalpriceApiKey("   ");
        goldPriceService.setMetalpriceApiServer("us");

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(
            "MetalpriceAPI key is not configured. Please set METALPRICE_API_KEY environment variable",
            exception.getMessage()
        );
    }

    @Test
    void updateGoldPrice_shouldSkipUpdate_whenMetalpriceApiDisabled() {
        // Given
        goldPriceService = new GoldPriceService(null, null, webClient);
        goldPriceService.setMetalpriceApiEnabled(false);

        // When
        goldPriceService.updateGoldPrice();

        // Then
        verify(webClient, never()).get();
    }

    @Test
    void updateGoldPrice_shouldSkipUpdate_whenEnabledAndApiKeyEmpty() {
        // Given
        goldPriceService = new GoldPriceService(null, null, webClient);
        goldPriceService.setMetalpriceApiEnabled(true);
        goldPriceService.setMetalpriceApiKey("");

        // When
        goldPriceService.updateGoldPrice();

        // Then
        verify(webClient, never()).get();
    }

    // Setter methods for testing
    private static class TestableGoldPriceService extends GoldPriceService {
        public TestableGoldPriceService(Object repo, Object exchangeRepo, WebClient webClient) {
            super((com.coffeecookies.homepage.repository.GoldPriceRepository) repo,
                  (com.coffeecookies.homepage.repository.ExchangeRateRepository) exchangeRepo,
                  webClient);
        }

        public void setMetalpriceApiEnabled(boolean enabled) {
            this.metalpriceApiEnabled = enabled;
        }

        public void setMetalpriceApiKey(String key) {
            this.metalpriceApiKey = key;
        }

        public void setMetalpriceApiServer(String server) {
            this.metalpriceApiServer = server;
        }
    }
}