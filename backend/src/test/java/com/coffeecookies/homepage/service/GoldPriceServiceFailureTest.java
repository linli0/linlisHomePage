package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.GoldPriceServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GoldPriceService API失败场景测试
 * 测试各种API失败场景的异常处理
 */
@ExtendWith(MockitoExtension.class)
class GoldPriceServiceFailureTest {

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
    void fetchGoldPriceFromAPI_shouldThrowException_whenApiResponseIsNull() {
        // Given
        mockWebClientNullResponse();

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(GoldPriceServiceTestHelper.TestErrorMessages.NULL_RESPONSE, exception.getMessage());
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenApiReturnsUnsuccessfulResponse() {
        // Given
        mockWebClientFailureResponse(105, "API quota exceeded");

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertTrue(exception.getMessage().contains("105"));
        assertTrue(exception.getMessage().contains("API quota exceeded"));
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenApiReturnsUnsuccessfulResponseWithErrorCode101() {
        // Given
        mockWebClientFailureResponse(101, "Invalid API key");

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertTrue(exception.getMessage().contains("101"));
        assertTrue(exception.getMessage().contains("Invalid API key"));
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenResponseMissingRatesField() {
        // Given
        mockWebClientResponse(GoldPriceServiceTestHelper.createMissingRatesResponseJson());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(GoldPriceServiceTestHelper.TestErrorMessages.MISSING_RATES, exception.getMessage());
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenResponseMissingUsdxauField() {
        // Given
        String responseWithoutUsdxau = """
            {
              "success": true,
              "timestamp": 1712175937,
              "base": "USD",
              "rates": {
                "USDXAG": 77.00
              }
            }
            """;
        mockWebClientResponse(responseWithoutUsdxau);

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(GoldPriceServiceTestHelper.TestErrorMessages.MISSING_USDXAU, exception.getMessage());
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenUsdxauIsZero() {
        // Given
        mockWebClientResponse(GoldPriceServiceTestHelper.createZeroUsdxauResponseJson());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(GoldPriceServiceTestHelper.TestErrorMessages.INVALID_USDXAU, exception.getMessage());
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenUsdxauIsNegative() {
        // Given
        mockWebClientResponse(GouldPriceServiceTestHelper.createNegativeUsdxauResponseJson());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(GoldPriceServiceTestHelper.TestErrorMessages.NEGATIVE_USDXAU, exception.getMessage());
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenApiReturnsSuccessFalseWithoutError() {
        // Given
        mockWebClientResponse(GoldPriceServiceTestHelper.createSuccessFalseNoErrorResponseJson());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertEquals(GoldPriceService.TestErrorMessages.UNSUCCESSFUL_RESPONSE, exception.getMessage());
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenWebClientThrowsException() {
        // Given
        mockWebClientThrowsException();

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        // 验证异常被正确处理并重新抛出
        assertNotNull(exception);
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenNetworkErrorOccurs() {
        // Given
        mockWebClientThrowsException(new RuntimeException("Network error: Connection refused"));

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertTrue(exception.getMessage().contains("Network error"));
    }

    @Test
    void fetchGoldPriceFromAPI_shouldThrowException_whenResponseTimeout() {
        // Given
        mockWebClientThrowsException(new WebClientResponseException(
            408, "Request Timeout", null, null
        ));

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> goldPriceService.fetchGoldPriceFromAPI()
        );

        assertNotNull(exception);
    }

    // Mock helper methods
    private void mockWebClientResponse(String mockResponse) {
        WebClient.RequestHeadersUriSpec headersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(headersSpec);
        when(headersSpec.uri(any())).thenReturn(requestSpec);
        when(requestSpec.retrieve(any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mockResponse);
    }

    private void mockWebClientFailureResponse(int errorCode, String errorMsg) {
        mockWebClientResponse(GoldPriceServiceTestHelper.createFailureApiResponseJson(errorCode, errorMsg));
    }

    private void mockWebClientNullResponse() {
        WebClient.RequestHeadersUriSpec headersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(headersSpec);
        when(headersSpec.uri(any())).thenReturn(requestSpec);
        when(requestSpec.retrieve(any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(null);
    }

    private void mockWebClientThrowsException() {
        mockWebClientThrowsException(new RuntimeException("Simulated API error"));
    }

    private void mockWebClientThrowsException(Exception e) {
        WebClient.RequestHeadersUriSpec headersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(headersSpec);
        when(headersSpec.uri(any())).thenReturn(requestSpec);
        when(requestSpec.retrieve(any())).thenThrow(e);
    }
}