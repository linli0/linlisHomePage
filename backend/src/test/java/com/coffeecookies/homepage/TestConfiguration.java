package com.coffeecookies.homepage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.MockWebServiceServer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试配置类
 * 提供测试专用的Bean配置
 */
@TestConfiguration
public class TestConfiguration {

    /**
     * Mock WebClient 配置
     * 用于测试时避免真实的外部API调用
     */
    @Bean
    @Primary
    public WebClient webClient() {
        return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
    }

    /**
     * Mock API服务器配置
     * 用于模拟外部API响应
     */
    @Bean
    @Primary
    public MockWebServiceServer mockApiServer() {
        MockWebServiceServer mockServer = MockWebServiceServer.bindToPort(0);
        
        // 注册Mock端点
        mockServer.stubFor(get("/api/test/price").willReturn(success()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mockSuccessResponse())));
        
        return mockServer;
    }

    /**
     * 模拟成功的API响应
     */
    @Bean
    @Primary
    public String mockSuccessResponse() {
        return """
            {
              "success": true,
              "timestamp": 1712175937,
              "base": "USD",
              "rates": {
                "USDXAU": 0.0002105
              }
            }
            """;
    }

    /**
     * 模拟失败的API响应
     */
    @Bean
    @Primary
    public String mockFailureResponse() {
        return """
            {
              "success": false,
              "error": {
                "code": 105,
                "info": "API quota exceeded"
              }
            }
            """;
    }

    /**
     * 模拟缺少rates字段的响应
     */
    @Bean
    @Primary
    public String mockMissingRatesResponse() {
        return """
            {
              "success": true,
              "timestamp": 1712175937,
              "base": "USD"
            }
            """;
    }

    /**
     * Mock控制器
     * 用于模拟MetalpriceAPI的端点
     */
    @RestController
    @Primary
    public static class MockApiController {

        private final Map<String, String> responses = new HashMap<>();

        public void setMockResponse(String endpoint, String response) {
            responses.put(endpoint, response);
        }

        public void clearMockResponses() {
            responses.clear();
        }

        @GetMapping("/v1/latest")
        public String getLatest(
                @RequestParam String api_key,
                @RequestParam String base,
                @RequestParam String currencies) {
            return responses.getOrDefault("/v1/latest", mockSuccessResponse());
        }
    }
}