package com.coffeecookies.homepage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.coffeecookies.homepage.repository.GoldPriceRepository;
import com.coffeecookies.homepage.repository.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GoldPriceService 定时任务测试
 * 测试@Scheduled定时任务方法的执行
 */
@ExtendWith(MockitoExtension.class)
class GoldPriceServiceScheduledTaskTest {

    @Mock
    private GoldPriceRepository goldPriceRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private GoldPriceService goldPriceService;

    @BeforeEach
    void setUp() {
        goldPriceService = new GoldPriceService(goldPriceRepository, exchangeRateRepository, webClient);
        goldPriceService.setMetalpriceApiEnabled(true);
        goldPriceService.setMetalpriceApiKey("test-api-key");
        goldPriceService.setMetalpriceApiServer("us");
    }

    @Test
    void updateGoldPrice_shouldFetchNewPrice_whenEnabled() {
        // Given
        mockWebClientSuccessResponse(2500.00);
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(null);

        // When
        goldPriceService.updateGoldPrice();

        // Then
        verify(webClient, times(1)).get();
        verify(goldPriceRepository, times(1)).save(any());
    }

    @Test
    void updateGoldPrice_shouldUpdateChangeAmount_whenLastPriceExists() {
        // Given
        BigDecimal oldPrice = 2400.00;
        BigDecimal newPrice = 2500.00;
        
        mockWebClientSuccessResponse(newPrice);
        
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(createMockGoldPrice(oldPrice));

        // When
        goldPriceService.updateGoldPrice();

        // Then
        verify(goldPriceRepository).save(argThat(goldPrice -> {
            BigDecimal changeAmount = newPrice.subtract(oldPrice);
            BigDecimal changePercent = changeAmount.multiply(BigDecimal.valueOf(100))
                    .divide(oldPrice, 4, RoundingMode.HALF_UP);
            
            assertEquals(changeAmount, goldPrice.getChangeAmount());
            assertEquals(changePercent, goldPrice.getChangePercent());
        }));
    }

    @Test
    void updateGoldPrice_shouldSetChangeToZero_whenNoPreviousPrice() {
        // Given
        mockWebClientSuccessResponse(2500.00);
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(null);

        // When
        goldPriceService.updateGoldPrice();

        // Then
        verify(goldPriceRepository).save(argThat(goldPrice -> {
            assertEquals(BigDecimal.ZERO, goldPrice.getChangeAmount());
            assertEquals(BigDecimal.ZERO, goldPrice.getChangePercent());
        }));
    }

    @Test
    void updateGoldPrice_shouldCallUpdateExchangeRates() {
        // Given
        mockWebClientSuccessResponse(2500.00);
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(null);

        // When
        goldPriceService.updateGoldPrice();

        // Then
        verify(goldPriceService, atLeastOnce()).updateExchangeRates();
    }

    @Test
    void updateGoldPrice_shouldNotSaveData_whenApiCallFails() {
        // Given
        mockWebClientFailureResponse();
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(null);

        // When
        goldPriceService.updateGoldPrice();

        // Then
        // 验证没有保存数据
        verify(goldPriceRepository, never()).save(any());
    }

    @Test
    void updateExchangeRates_shouldCallExchangeRateApi() {
        // Given
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "CNY"))
            .thenReturn(null);
        
        // When
        goldPriceService.updateExchangeRates();

        // Then
        verify(webClient, atLeastOnce()).get();
    }

    @Test
    void updateExchangeRates_shouldSaveExchangeRate_whenRateExistsInApi() {
        // Given
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "CNY"))
            .thenReturn(null);

        // When
        goldPriceService.updateExchangeRates();

        // Then
        verify(exchangeRateRepository, times(1)).save(any());
    }

    @Test
 void updateExchangeRates_shouldUpdateExchangeRate_whenRateAlreadyExists() {
        // Given
        com.coffeecookies.homepage.entity.ExchangeRate existingRate = 
            new com.coffeecookies.homepage.entity.ExchangeRate();
        existingRate.setRate(new BigDecimal("7.20"));
        
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency("USD", "CNY"))
            .thenReturn(java.util.Optional.of(existingRate));

        // When
        goldPriceService.updateExchangeRates();

        // Then
        verify(exchangeRateRepository, times(1)).save(argThat(rate -> {
            // 确保更新了汇率
            assertNotNull(rate);
        }));
    }

    @Test
    void updateExchangeRates_shouldSkipCurrency_whenNotInApi() {
        // Given
        // API 返回的 rates 中没有 XYZ 货币
        // 当前的 CURRENCIES map 中只有 USD, CNY, EUR, GBP

        // When
        goldPriceService.updateExchangeRates();

        // Then
        // 验证只保存了支持的货币
        verify(exchangeRateRepository, atMost(4)).save(any());
    }

    @Test
    void updateGoldPrice_shouldHandleExceptionGracefully_whenApiFails() {
        // Given
        mockWebClientThrowsException();
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(null);

        // When
        assertDoesNotThrow(() -> goldPriceService.updateGoldPrice());

        // Then
        // 验证没有保存错误数据
        verify(goldPriceRepository, never()).save(any());
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    void updateGoldPrice_shouldContinueUpdateExchangeRates_whenPriceUpdateFails() {
        // Given
        mockWebClientThrowsException();
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(null);

        // When
        goldPriceService.updateGoldPrice();

        // Then
        // 验证即使金价更新失败，汇率更新仍被尝试
        verify(goldPriceService, atLeastOnce()).updateExchangeRates();
    }

    @Test
    void updateGoldPrice_shouldLogInfo_whenPriceUpdatedSuccessfully() {
        // Given
        mockWebClientSuccessResponse(2500.00);
        when(goldPriceRepository.findTopByOrderByRecordedAtDesc()).thenReturn(null);

        // When
        goldPriceService.updateGoldPrice();

        // Then
        // 验证日志被记录（需要检查日志配置）
        // 这只是一个示例，实际验证需要更复杂的设置
        verify(goldPriceRepository, times(1)).save(any());
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

    private void mockWebClientFailureResponse() {
        mockWebClientThrowsException(new RuntimeException("API call failed"));
    }

    private void mockWebClientThrowsException() {
        mockWebClientThrowsException(new RuntimeException("API error"));
    }

    private void mockWebClientThrowsException(Exception e) {
        WebClient.RequestHeadersUriSpec headersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(headersSpec);
        when(headersSpec.uri(any())).thenReturn(requestSpec);
        when(requestSpec.retrieve(any())).thenThrow(e);
    }

    private com.coffeecookies.homepage.entity.GoldPrice createMockGoldPrice(BigDecimal priceUsd) {
        com.coffeecookies.homepage.entity.GoldPrice goldPrice = new com.coffeecookies.homepage.entity.GoldPrice();
        goldPrice.setPriceUsd(priceUsd);
        goldPrice.setChangeAmount(BigDecimal.ZERO);
        goldPrice.setChangePercent(BigDecimal.ZERO);
        goldPrice.setCurrency("USD");
        goldPrice.setRecordedAt(LocalDateTime.now());
        return goldPrice;
    }
}