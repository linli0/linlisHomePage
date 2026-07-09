package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.config.XiaomiSpeakerConfig;
import com.coffeecookies.homepage.dto.AIChatRequest;
import com.coffeecookies.homepage.dto.XiaomiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class XiaomiSpeakerService {

    private final XiaomiSpeakerConfig xiaomiSpeakerConfig;
    private final WebClient.Builder webClientBuilder;
    private final AIService aiService;
    
    public XiaomiSpeakerConfig getXiaomiSpeakerConfig() {
        return xiaomiSpeakerConfig;
    }
    
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    /**
     * 发送 TTS 播报到小爱音箱
     * LX06 命令映射: tts=[5,1]
     */
    public XiaomiResponse tts(String text) {
        if (!xiaomiSpeakerConfig.isEnabled()) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小爱音箱集成功能未启用")
                    .build();
        }
        
        try {
            // 构建小米云 API 请求
            Map<String, Object> requestBody = Map.of(
                "method", "set_player_command",
                "params", Map.of(
                    "command", new int[]{5, 1}, // tts command for LX06
                    "text", text
                )
            );
            
            WebClient webClient = createMiWebClient();
            
            // 调用小米云 API
            Map response = webClient.post()
                .uri("/mina_api/device/" + xiaomiSpeakerConfig.getDeviceIp() + "/player")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
                
            if (response != null && Boolean.TRUE.equals(response.get("result"))) {
                return XiaomiResponse.builder()
                        .success(true)
                        .message("TTS 播报成功")
                        .data(response)
                        .build();
            } else {
                return XiaomiResponse.builder()
                        .success(false)
                        .message("TTS 播报失败: " + (response != null ? response.get("message") : "未知错误"))
                        .build();
            }
            
        } catch (WebClientResponseException e) {
            log.error("小米云 API 返回错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小米云 API 错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString())
                    .build();
        } catch (Exception e) {
            log.error("发送 TTS 到小爱音箱失败: {}", e.getMessage(), e);
            return XiaomiResponse.builder()
                    .success(false)
                    .message("发送 TTS 失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 唤醒小爱音箱
     * LX06 命令映射: wake=[5,3]
     */
    public XiaomiResponse wake() {
        if (!xiaomiSpeakerConfig.isEnabled()) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小爱音箱集成功能未启用")
                    .build();
        }
        
        try {
            // 构建小米云 API 请求
            Map<String, Object> requestBody = Map.of(
                "method", "set_player_command",
                "params", Map.of(
                    "command", new int[]{5, 3} // wake command for LX06
                )
            );
            
            WebClient webClient = createMiWebClient();
            
            // 调用小米云 API
            Map response = webClient.post()
                .uri("/mina_api/device/" + xiaomiSpeakerConfig.getDeviceIp() + "/player")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
                
            if (response != null && Boolean.TRUE.equals(response.get("result"))) {
                return XiaomiResponse.builder()
                        .success(true)
                        .message("唤醒小爱成功")
                        .data(response)
                        .build();
            } else {
                return XiaomiResponse.builder()
                        .success(false)
                        .message("唤醒小爱失败: " + (response != null ? response.get("message") : "未知错误"))
                        .build();
            }
            
        } catch (WebClientResponseException e) {
            log.error("小米云 API 返回错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小米云 API 错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString())
                    .build();
        } catch (Exception e) {
            log.error("唤醒小爱音箱失败: {}", e.getMessage(), e);
            return XiaomiResponse.builder()
                    .success(false)
                    .message("唤醒小爱失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * AI 对话核心逻辑
     * 1. 用户发送消息 -> wake 小爱 -> 暂停小爱默认回复
     * 2. 调用 Ollama API 获取 AI 回复
     * 3. 使用 TTS 播报 AI 回复
     */
    public XiaomiResponse chatWithAi(String message, boolean stream) {
        if (!xiaomiSpeakerConfig.isEnabled()) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小爱音箱集成功能未启用")
                    .build();
        }
        
        try {
            // 步骤 1: 唤醒小爱
            XiaomiResponse wakeResponse = wake();
            if (!wakeResponse.isSuccess()) {
                return wakeResponse;
            }
            
            // 步骤 2: 调用 Ollama API 获取 AI 回复
            // 使用默认模型（可以根据需要配置）
            String defaultModel = "llama3"; // 可以从配置中读取
            
            AIChatRequest aiRequest = AIChatRequest.builder()
                    .model(defaultModel)
                    .prompt(message)
                    .stream(stream)
                    .build();
                    
            Map<String, Object> aiResponse;
            if (stream) {
                // 对于流式响应，我们需要特殊处理
                // 这里简化处理，实际可能需要 WebSocket 或其他机制
                aiResponse = aiService.chatNonStream(aiRequest);
            } else {
                aiResponse = aiService.chatNonStream(aiRequest);
            }
            
            // 提取 AI 回复文本
            String aiReply = extractAiReply(aiResponse);
            if (aiReply == null || aiReply.trim().isEmpty()) {
                return XiaomiResponse.builder()
                        .success(false)
                        .message("AI 回复为空")
                        .build();
            }
            
            // 步骤 3: 使用 TTS 播报 AI 回复
            return tts(aiReply);
            
        } catch (Exception e) {
            log.error("AI 对话失败: {}", e.getMessage(), e);
            return XiaomiResponse.builder()
                    .success(false)
                    .message("AI 对话失败: " + e.getMessage())
                    .build();
        }
    }
    
    private String extractAiReply(Map<String, Object> aiResponse) {
        if (aiResponse == null) {
            return null;
        }
        
        // 从 Ollama 响应中提取回复文本
        Object response = aiResponse.get("response");
        if (response instanceof String) {
            return (String) response;
        }
        
        // 如果没有 response 字段，尝试其他可能的字段
        for (Object value : aiResponse.values()) {
            if (value instanceof String && !((String) value).isEmpty()) {
                return (String) value;
            }
        }
        
        return null;
    }
    
    private WebClient createMiWebClient() {
        // 创建小米云 API 的 WebClient
        // 需要处理认证：使用小米账号登录获取 serviceToken
        WebClient.Builder builder = webClientBuilder
                .baseUrl("https://api.mina.mi.com");
                
        // 添加认证头（需要实现小米账号登录获取 token 的逻辑）
        // 这里简化处理，实际需要更复杂的认证流程
        if (xiaomiSpeakerConfig.getMiAccountUsername() != null && 
            xiaomiSpeakerConfig.getMiAccountPassword() != null) {
            // TODO: 实现小米账号登录获取 serviceToken 的逻辑
            // 这通常需要调用小米的登录 API 并处理 cookies/serviceToken
            // 由于复杂性，这里先使用 deviceToken 进行局域网控制
        }
        
        return builder.build();
    }
    
    /**
     * 局域网控制方法（使用 deviceToken）
     * 适用于用户已有 Token，局域网环境的情况
     */
    public XiaomiResponse ttsLocal(String text) {
        if (!xiaomiSpeakerConfig.isEnabled()) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小爱音箱集成功能未启用")
                    .build();
        }
        
        if (xiaomiSpeakerConfig.getDeviceIp() == null || 
            xiaomiSpeakerConfig.getDeviceToken() == null) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("缺少设备 IP 或 Token 配置")
                    .build();
        }
        
        try {
            // 构建局域网控制请求
            // LX06 局域网协议：{"method":"set_player_command","params":{"command":[5,1],"text":"hello"}}
            Map<String, Object> requestBody = Map.of(
                "method", "set_player_command",
                "params", Map.of(
                    "command", new int[]{5, 1}, // tts command for LX06
                    "text", text
                )
            );
            
            WebClient webClient = webClientBuilder
                    .baseUrl("http://" + xiaomiSpeakerConfig.getDeviceIp())
                    .build();
            
            // 添加认证头
            HttpHeaders headers = new HttpHeaders();
            headers.set("MIOT-DEVICE-ID", xiaomiSpeakerConfig.getDeviceToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map response = webClient.post()
                .uri("/miotspec/action")
                .headers(h -> h.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
                
            if (response != null) {
                return XiaomiResponse.builder()
                        .success(true)
                        .message("TTS 播报成功")
                        .data(response)
                        .build();
            } else {
                return XiaomiResponse.builder()
                        .success(false)
                        .message("TTS 播报失败: 无响应")
                        .build();
            }
            
        } catch (WebClientResponseException e) {
            log.error("局域网控制 API 返回错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return XiaomiResponse.builder()
                    .success(false)
                    .message("局域网控制错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString())
                    .build();
        } catch (Exception e) {
            log.error("局域网 TTS 控制失败: {}", e.getMessage(), e);
            return XiaomiResponse.builder()
                    .success(false)
                    .message("局域网 TTS 失败: " + e.getMessage())
                    .build();
        }
    }
    
    public XiaomiResponse wakeLocal() {
        if (!xiaomiSpeakerConfig.isEnabled()) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小爱音箱集成功能未启用")
                    .build();
        }
        
        if (xiaomiSpeakerConfig.getDeviceIp() == null || 
            xiaomiSpeakerConfig.getDeviceToken() == null) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("缺少设备 IP 或 Token 配置")
                    .build();
        }
        
        try {
            // 构建局域网控制请求
            Map<String, Object> requestBody = Map.of(
                "method", "set_player_command",
                "params", Map.of(
                    "command", new int[]{5, 3} // wake command for LX06
                )
            );
            
            WebClient webClient = webClientBuilder
                    .baseUrl("http://" + xiaomiSpeakerConfig.getDeviceIp())
                    .build();
            
            // 添加认证头
            HttpHeaders headers = new HttpHeaders();
            headers.set("MIOT-DEVICE-ID", xiaomiSpeakerConfig.getDeviceToken());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map response = webClient.post()
                .uri("/miotspec/action")
                .headers(h -> h.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
                
            if (response != null) {
                return XiaomiResponse.builder()
                        .success(true)
                        .message("唤醒小爱成功")
                        .data(response)
                        .build();
            } else {
                return XiaomiResponse.builder()
                        .success(false)
                        .message("唤醒小爱失败: 无响应")
                        .build();
            }
            
        } catch (WebClientResponseException e) {
            log.error("局域网控制 API 返回错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return XiaomiResponse.builder()
                    .success(false)
                    .message("局域网控制错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString())
                    .build();
        } catch (Exception e) {
            log.error("局域网唤醒控制失败: {}", e.getMessage(), e);
            return XiaomiResponse.builder()
                    .success(false)
                    .message("局域网唤醒失败: " + e.getMessage())
                    .build();
        }
    }
    
    public XiaomiResponse chatWithAiLocal(String message) {
        if (!xiaomiSpeakerConfig.isEnabled()) {
            return XiaomiResponse.builder()
                    .success(false)
                    .message("小爱音箱集成功能未启用")
                    .build();
        }
        
        try {
            // 步骤 1: 唤醒小爱
            XiaomiResponse wakeResponse = wakeLocal();
            if (!wakeResponse.isSuccess()) {
                return wakeResponse;
            }
            
            // 步骤 2: 调用 Ollama API 获取 AI 回复
            String defaultModel = "llama3";
            AIChatRequest aiRequest = AIChatRequest.builder()
                    .model(defaultModel)
                    .prompt(message)
                    .stream(false)
                    .build();
                    
            Map<String, Object> aiResponse = aiService.chatNonStream(aiRequest);
            String aiReply = extractAiReply(aiResponse);
            
            if (aiReply == null || aiReply.trim().isEmpty()) {
                return XiaomiResponse.builder()
                        .success(false)
                        .message("AI 回复为空")
                        .build();
            }
            
            // 步骤 3: 使用 TTS 播报 AI 回复
            return ttsLocal(aiReply);
            
        } catch (Exception e) {
            log.error("AI 对话失败: {}", e.getMessage(), e);
            return XiaomiResponse.builder()
                    .success(false)
                    .message("AI 对话失败: " + e.getMessage())
                    .build();
        }
    }
}