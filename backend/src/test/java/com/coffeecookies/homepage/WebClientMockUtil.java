package com.coffeecookies.homepage;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.MockWebServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * WebClient Mock 工具类
 * 提供WebClient的Mock功能，避免真实API调用
 */
public class WebClientMockUtil {

    /**
     * 创建Mock WebClient，返回预设的响应
     *
     * @param mockResponse 要返回的JSON响应字符串
     * @return 配置好的WebClient
     */
    public static WebClient createMockWebClient(String mockResponse) {
        MockWebServiceServer mockServer = MockWebServiceServer.bindToPort(0);
        
        mockServer.stubFor(org.springframework.http.HttpMethod.GET, "/v1/latest")
            .willReturn(org.springframework.test.web.reactive.server.MockResponse
                .withBody(mockResponse)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE));

        return WebClient.builder()
            .baseUrl("http://localhost:" + mockServer.getPort())
            .build();
    }

    /**
     * 创建返回成功的WebClient
     *
     * @param usdPerTroyOunce 美元每盎司价格
     * @return 配置好的WebClient
     */
    public static WebClient createSuccessWebClient(double usdPerTroyOunce) {
        // API返回的是 USDXAU (1 USD = X XAU)，需要转换
        double usdXau = 1.0 / usdPerTroyOunce;
        
        JSONObject response = new JSONObject();
        response.put("success", true);
        response.put("timestamp", 1712175937);
        response.put("base", "USD");
        
        JSONObject rates = new JSONObject();
        rates.put("USDXAU", usdXau);
        response.put("rates", rates);

        return createMockWebClient(response.toString());
    }

    /**
     * 创建返回失败的WebClient
     *
     * @param errorCode 错误代码
     * @param errorMsg 错误信息
     * @return 配置好的WebClient
     */
    public static WebClient createFailureWebClient(int errorCode, String errorMsg) {
        JSONObject response = new JSONObject();
        response.put("success", false);
        
        JSONObject error = new JSONObject();
        error.put("code", errorCode);
        error.put("info", errorMsg);
        response.put("error", error);

        return createMockWebClient(response.toString());
    }

    /**
     * 创建返回null响应的WebClient
     *
     * @return 配置好的WebClient
     */
    public static WebClient createNullResponseWebClient() {
        MockWebServiceServer mockServer = MockWebServiceServer.bindToPort(0);
        
        mockServer.stubFor(org.springframework.http.HttpMethod.GET, "/v1/latest")
            .willReturn(org.springframework.test.web.reactive.server.MockResponse
                .withStatus(HttpStatus.OK)
                .withBody((String) null));

        return WebClient.builder()
            .baseUrl("http://localhost:" + mockServer.getPort())
            .build();
    }

    /**
     * 创建超时响应的WebClient
     *
     * @return 配置好的WebClient
     */
    public static WebClient createTimeoutWebClient() {
        MockWebServiceServer mockServer = MockWebServiceServer.bindToPort(0);
        
        mockServer.stubFor(org.springframework.http.HttpMethod.GET, "/v1/latest")
            .willReturn(org.springframework.test.web.reactive.server.MockResponse
                .withStatus(HttpStatus.REQUEST_TIMEOUT));

        return WebClient.builder()
            .baseUrl("http://localhost:" + mockServer.getPort())
            .build();
    }
}