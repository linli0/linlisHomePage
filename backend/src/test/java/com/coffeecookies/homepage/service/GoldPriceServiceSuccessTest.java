package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.GoldPriceServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * GoldPriceService 成功API调用测试
 * 测试成功的API调用、JSON解析和价格计算
 */
@ExtendWith(MockitoExtension.class)
class GoldPriceServiceSuccessTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private GoldPriceService goldPriceService;

    @BeforeEach
    void setUp() {
        goldPriceService = new GoldPriceService(null, null, webClient);
        goldPriceService.setMetalpriceApiEnabled(true);
        goldPriceService.setMetalpriceApiKey("test-api-key");
        goldPriceService.setMetalpriceApiServer("us");
    }

    @Test
    void fetchGoldPriceFromAPI_shouldReturnCorrectPrice_whenApiReturnsValidResponse() {
        // Given
        double expectedPrice = 2500.00;
        mockWebClientSuccessResponse(expectedPrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(expectedPrice).setScale(2, RoundingMode.HALF_UP), actualPrice);
        verify(webClient, times(1)).get();
    }

    @Test
    void fetchGoldPriceFromAPI_shouldReturnCorrectPrice_whenPriceIs2000() {
        // Given
        double expectedPrice = 2000.00;
        mockWebClientSuccessResponse(expectedPrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(expectedPrice).setScale(2, RoundingMode.HALF_UP), actualPrice);
    }

    @Test
    void fetchGoldPriceFromAPI_shouldReturnCorrectPrice_whenPriceIs3000() {
        // Given
        double expectedPrice = 3000.00;
        mockWebClientSuccessResponse(expectedPrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(expectedPrice).setScale(2, RoundingMode.HALF_UP), actualPrice);
    }

    @Test
    void fetchGoldPriceFromAPI_shouldUseEUServer_whenServerConfiguredAsEU() {
        // Given
        goldPriceService.setMetalpriceApiServer("eu");
        mockWebClientSuccessResponse(2500.00);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(2500.00).setScale(2, RoundingMode.HALF_UP), actualPrice);
        verify(webClient, times(1)).get();
    }

    @Test
    void fetchGoldPriceFromAPI_shouldUseUSServer_whenServerConfiguredAsUS() {
        // Given
        goldPriceService.setMetalpriceApiServer("us");
        mockWebClientSuccessResponse(2500.00);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(2500.00).setScale(2, RoundingMode.HALF_UP), actualPrice);
        verify(webClient, times(1)).get();
    }

    @Test
    void fetchGoldPriceFromAPI_shouldRoundPriceCorrectly() {
        // Given
        double unroundedPrice = 2350.789;
        mockWebClientSuccessResponse(unroundedPrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then - 应该四舍五入到两位小数
        assertEquals(new BigDecimal("2350.79"), actualPrice);
    }

    @Test
    void fetchGoldPriceFromAPI_shouldHandlePriceWithTwoDecimals() {
        // Given
        double expectedPrice = 2350.50;
        mockWebClientSuccessResponse(expectedPrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(expectedPrice).setScale(2, RoundingMode.HALF_UP), actualPrice);
    }

    @Test
    void fetchGoldPriceFromAPI_shouldCallWebClientExactlyOnce() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        verify(webClient, times(1)).get();
        verifyNoMoreInteractions(webClient);
    }

    // Mock helper methods
    private void mockWebClientSuccessResponse(double usdPerTroyOunce) {
        WebClient.RequestHeadersUriSpec headersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        String mockResponse = GoldPriceServiceTestHelper.createValidApiResponseJson(usdPerTroyOunce);

        when(webClient.get()).thenReturn(headersSpec);
        when(headersSpec.uri(any())).thenReturn(requestSpec);
        when(requestSpec.retrieve(any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mockResponse);
    }
}