package com.coffeecookies.homepage;

import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

/**
 * GoldPriceService 测试辅助类
 * 提供Mock WebClient和测试数据
 */
public class GoldPriceServiceTestHelper {

    /**
     * 创建Mock WebClient，返回成功响应
     */
    public static WebClient createMockWebClientSuccess(double usdPerTroyOunce) {
        WebClient mockWebClient = mock(WebClient.class);
        
        when(mockWebClient.get())
            .thenReturn(any(org.springframework.web.reactive.function.client.WebClient.Request.class));
        
        return mockWebClient;
    }

    /**
     * 创建Mock WebClient，返回失败响应
     */
    public static WebClient createMockWebClientFailure() {
        WebClient mockWebClient = mock(WebClient.class);
        
        WebClient.RequestHeadersUriSpec headersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        
        when(mockWebClient.get()).thenReturn(headersSpec);
        when(headersSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve(any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenThrow(new WebClientResponseException(
            500, "Internal Server Error", null, null));

        return mockWebClient;
    }

    /**
     * 创建Mock WebClient，返回null响应
     */
    public static WebClient createMockWebClientNullResponse() {
        WebClient mockWebClient = mock(WebClient.class);
        
        WebClient.RequestHeadersUriSpec headersSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        
        when(mockWebClient.get()).thenReturn(headersSpec);
        when(headersSpec.uri(anyString())).thenReturn(requestSpec);
        when(requestSpec.retrieve(any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(null);

        return mockWebClient;
    }

    /**
     * 创建有效的API响应JSON
     */
    public static String createValidApiResponseJson(double usdPerTroyOunce) {
        // API返回的是 USDXAU (1 USD = X XAU)，需要转换
        double usdXau = 1.0 / usdPerTroyOunce;
        
        return String.format("""
            {
              "success": true,
              "timestamp": 1712175937,
              "base": "USD",
              "rates": {
                "USDXAU": %.10f
              }
            }
            """, usdXau);
    }

    /**
     * 创建失败的API响应JSON
     */
    public static String createFailureApiResponseJson(int errorCode, String errorMsg) {
        return String.format("""
            {
              "success": false,
              "error": {
                "code": %d,
                "info": "%s"
              }
            }
            """, errorCode, errorMsg);
    }

    /**
     * 创建缺少rates字段的API响应JSON
     */
    public static String createMissingRatesResponseJson() {
        return """
            {
              "success": true,
              "timestamp": 1712175937,
              "base": "USD"
            }
            """;
    }

    /**
     * 创建success为false但无error字段的API响应JSON
     */
    public static String createSuccessFalseNoErrorResponseJson() {
        return """
            {
              "success": false,
              "timestamp": 1712175937,
              "base": "USD"
            }
            """;
    }

    /**
     * 创建USDXAU为0的API响应JSON
     */
    public static String createZeroUsdxauResponseJson() {
        return """
            {
              "success": true,
              "timestamp": 1712175937,
              "base": "USD",
              "rates": {
                "USDXAU": 0.0
              }
            }
            """;
    }

    /**
     * 创建USDXAU为负数的API响应JSON
     */
    public static String createNegativeUsdxauResponseJson() {
        return """
            {
              "success": true,
              "timestamp": 1712175937,
              "base": "USD",
              "rates": {
                "USDXAU": -0.0002105
              }
            }
            """;
    }

    /**
     * 测试用的价格常量
     */
    public static class TestPrices {
        public static final BigDecimal PRICE_2000_00 = new BigDecimal("2000.00");
        public static final BigDecimal PRICE_2500_00 = new BigDecimal("2500.00");
        public static final BigDecimal PRICE_3000_00 = new BigDecimal("3000.00");
        public static final BigDecimal INVALID_PRICE = new BigDecimal("-1.00");
        public static final BigDecimal ZERO_PRICE = BigDecimal.ZERO;
    }

    /**
     * 测试用的异常消息
     */
    public static class TestErrorMessages {
        public static final String NOT_ENABLED = "MetalpriceAPI is not enabled. Please set METALPRICE_ENABLED=true";
        public static final String NO_API_KEY = "MetalpriceAPI key is not configured. Please set METALPRICE_API_KEY environment variable";
        public static final String NULL_RESPONSE = "MetalpriceAPI returned null response";
        public static final String UNSUCCESSFUL_RESPONSE = "MetalpriceAPI error: API call failed";
        public static final String MISSING_RATES = "MetalpriceAPI response missing rates field";
        public static final String MISSING_USDXAU = "MetalpriceAPI response missing USDXAU field";
        public static final String INVALID_USDXAU = "MetalpriceAPI returned invalid USDXAU value: 0.0";
        public static final String NEGATIVE_USDXAU = "MetalpriceAPI returned invalid USDXAU value: -0.0002105";
        public static final String NETWORK_ERROR = "Network error: Connection refused";
    }
}