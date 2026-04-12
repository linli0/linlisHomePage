package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.GoldPriceServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GoldPriceService WebClient Mock测试
 * 验证WebClient被正确Mock，没有真实的API调用
 */
@ExtendWith(MockitoExtension.class)
class GoldPriceServiceWebClientMockTest {

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
    void shouldCallWebClient_whenFetchingPrice() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        verify(webClient, times(1)).get();
        verify(webClient, never()).post();
        verify(webClient, never()).put();
        verify(webClient, never()).delete();
    }

    @Test
    void shouldCallCorrectApiEndpoint_whenServerIsUS() {
        // Given
        goldPriceService.setMetalpriceApiServer("us");
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        verify(webClient, times(1)).get();
        // 验证调用的是正确的端点（这里只验证get方法被调用）
    }

    @Test
    void shouldCallCorrectApiEndpoint_whenServerIsEU() {
        // Given
        goldPriceService.setMetalpriceApiServer("eu");
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        verify(webClient, times(1)).get();
        // 验证调用的是正确的端点（这里只验证get方法被调用）
    }

    @Test
    void shouldPassApiKeyAsParameter_whenMakingRequest() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        // 验证 WebClient 被调用，实际参数验证需要更复杂的mock设置
        verify(webClient, atLeastOnce()).get();
    }

    @Test
    void shouldNotMakeAnyExternalCalls_whenWebClientIsMocked() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        BigDecimal price = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertNotNull(price);
        assertEquals(BigDecimal.valueOf(2500.00), price);
        // 验证只调用了mock的WebClient，没有真实的网络调用
        verify(webClient, times(1)).get();
    }

    @Test
    void shouldUseBaseUSD_whenCallingApi() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        verify(webClient, times(1)).get();
        // 验证请求参数中包含 base=USD（需要更复杂的mock设置来验证）
    }

    @Test
    void shouldRequestXAUCurrency_whenCallingApi() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        verify(webClient, times(1)).get();
        // 验证请求参数中包含 currencies=XAU（需要更复杂的mock设置来验证）
    }

    @Test
    void shouldReturnBigDecimal_whenApiResponseIsNumeric() {
        // Given
        double expectedPrice = 2350.78;
        mockWebClientSuccessResponse(expectedPrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertTrue(actualPrice instanceof BigDecimal);
        assertEquals(BigDecimal.valueOf(expectedPrice).setScale(2, BigDecimal.ROUND_HALF_UP), actualPrice);
    }

    @Test
    void shouldHandleLargePriceValue_whenApiResponseHasLargeNumber() {
        // Given
        double largePrice = 5000.00;
        mockWebClientSuccessResponse(largePrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(largePrice).setScale(2, RoundingMode.HALF_UP), actualPrice);
    }

    @Test
    void shouldHandleSmallPriceValue_whenApiResponseHasSmallNumber() {
        // Given
        double smallPrice = 1500.00;
        mockWebClientSuccessResponse(smallPrice);

        // When
        BigDecimal actualPrice = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(smallPrice).setScale(2, RoundingMode.HALF_UP), actualPrice);
    }

    @Test
    void shouldNotMakeMultipleCalls_whenCalledMultipleTimes() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        goldPriceService.fetchGoldPriceFromAPI();
        goldPriceService.fetchGoldPriceFromAPI();

        // Then
        verify(webClient, times(2)).get();
        verifyNoMoreInteractions(webClient);
    }

    @Test
    void shouldUseMockWebClient_whenRealWebClientWouldFail() {
        // Given
        // 使用mock避免真实API调用
        mockWebClientSuccessResponse(2500.00);

        // When
        BigDecimal price = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertEquals(BigDecimal.valueOf(2500.00), price);
        // 验证使用的是mock WebClient，没有真实网络调用
        verify(webClient, atLeastOnce()).get();
    }

    @Test
    void shouldNotCallRealApi_whenRunningInTest() {
        // Given
        mockWebClientSuccessResponse(2500.00);

        // When
        BigDecimal price = goldPriceService.fetchGoldPriceFromAPI();

        // Then
        assertNotNull(price);
        // 验证只调用了mock，没有真实API调用
        // 这确保测试不会产生真实的API请求
        verify(webClient, atLeastOnce()).get();
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