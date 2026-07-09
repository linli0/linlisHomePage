package com.coffeecookies.homepage.service;

import com.coffeecookies.homepage.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {
    
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    @Value("${ollama.remote-url:}")
    private String ollamaRemoteUrl;
    
    @Value("${ollama.remote-enabled:false}")
    private boolean remoteEnabled;
    
    @Value("${ollama.auto-switch:true}")
    private boolean autoSwitch;
    
    // 当前使用的 URL（自动切换）
    private String currentOllamaUrl;
    
    private final WebClient.Builder webClientBuilder;
    
    /**
     * 获取当前有效的 Ollama URL
     * 如果本地不在线且启用了自动切换，则使用远程 URL
     */
    private String getEffectiveOllamaUrl() {
        if (currentOllamaUrl != null) {
            return currentOllamaUrl;
        }
        
        // 先检查本地状态
        boolean localOnline = checkOllamaOnline(ollamaBaseUrl);
        
        if (localOnline) {
            currentOllamaUrl = ollamaBaseUrl;
            log.info("Using local Ollama at: {}", ollamaBaseUrl);
        } else if (remoteEnabled && ollamaRemoteUrl != null && !ollamaRemoteUrl.isEmpty()) {
            // 检查远程是否在线
            boolean remoteOnline = checkOllamaOnline(ollamaRemoteUrl);
            if (remoteOnline) {
                currentOllamaUrl = ollamaRemoteUrl;
                log.info("Local Ollama offline, switched to remote: {}", ollamaRemoteUrl);
            } else {
                log.warn("Both local and remote Ollama are offline");
                currentOllamaUrl = ollamaBaseUrl; // 默认使用本地
            }
        } else {
            currentOllamaUrl = ollamaBaseUrl;
        }
        
        return currentOllamaUrl;
    }
    
    /**
     * 检查指定 URL 的 Ollama 是否在线
     */
    private boolean checkOllamaOnline(String url) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(url).build();
            webClient.get()
                .uri("/api/tags")
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(3));
            return true;
        } catch (Exception e) {
            log.debug("Ollama at {} is offline: {}", url, e.getMessage());
            return false;
        }
    }
    
    /**
     * 重置 Ollama URL（用于强制重新检测）
     */
    public void resetOllamaUrl() {
        currentOllamaUrl = null;
    }
    
    public AIModelsResponse getModels() {
        try {
            String effectiveUrl = getEffectiveOllamaUrl();
            WebClient webClient = webClientBuilder
                .baseUrl(effectiveUrl)
                .build();
            
            OllamaModelsResponse ollamaResponse = webClient.get()
                .uri("/api/tags")
                .retrieve()
                .bodyToMono(OllamaModelsResponse.class)
                .block(Duration.ofSeconds(10));
            
            if (ollamaResponse == null || ollamaResponse.getModels() == null) {
                return new AIModelsResponse();
            }
            
            // Map Ollama models to our AIModel format
            List<AIModel> aiModels = ollamaResponse.getModels().stream()
                .map(ollamaModel -> {
                    AIModel aiModel = new AIModel();
                    aiModel.setName(ollamaModel.getName());
                    aiModel.setSize(ollamaModel.getSize());
                    aiModel.setModifiedAt(ollamaModel.getModifiedAt());
                    
                    if (ollamaModel.getDetails() != null) {
                        aiModel.setParameterSize(ollamaModel.getDetails().getParameterSize());
                        aiModel.setFamily(ollamaModel.getDetails().getFamily());
                        aiModel.setFormat(ollamaModel.getDetails().getFormat());
                    }
                    
                    return aiModel;
                })
                .collect(Collectors.toList());
            
            AIModelsResponse response = new AIModelsResponse();
            response.setModels(aiModels);
            return response;
                
        } catch (WebClientResponseException e) {
            log.error("Ollama API returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ollama API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to connect to Ollama service at {}: {}", ollamaBaseUrl, e.getMessage());
            throw new RuntimeException("Failed to connect to Ollama service. Please ensure Ollama is running at " + ollamaBaseUrl);
        }
    }
    
    public StreamingResponseBody chatStream(AIChatRequest request) {
        String effectiveUrl = getEffectiveOllamaUrl();
        WebClient webClient = webClientBuilder
            .baseUrl(effectiveUrl)
            .build();
        
        // Create request body for streaming
        Map<String, Object> requestBody = Map.of(
            "model", request.getModel(),
            "prompt", request.getPrompt(),
            "stream", true,
            "context", request.getContext() != null ? request.getContext() : Collections.emptyList()
        );
        
        return outputStream -> {
            try {
                webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(byte[].class)
                    .doOnNext(bytes -> {
                        try {
                            outputStream.write(bytes);
                        } catch (java.io.IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .doOnError(error -> {
                        log.error("Streaming error: {}", error.getMessage(), error);
                        throw new RuntimeException("Streaming error: " + error.getMessage());
                    })
                    .blockLast(Duration.ofMinutes(5)); // Allow up to 5 minutes for response
                    
                outputStream.flush();
            } catch (Exception e) {
                log.error("Error during streaming chat: {}", e.getMessage(), e);
                throw new RuntimeException("Streaming error: " + e.getMessage());
            }
        };
    }
    
    public Map<String, Object> chatNonStream(AIChatRequest request) {
        String effectiveUrl = getEffectiveOllamaUrl();
        WebClient webClient = webClientBuilder
            .baseUrl(effectiveUrl)
            .build();
        
        // Create request body for non-streaming
        Map<String, Object> requestBody = Map.of(
            "model", request.getModel(),
            "prompt", request.getPrompt(),
            "stream", false,
            "context", request.getContext() != null ? request.getContext() : Collections.emptyList()
        );
        
        try {
            return webClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Error during non-streaming chat: {}", e.getMessage(), e);
            throw new RuntimeException("Non-streaming chat error: " + e.getMessage());
        }
    }
    
    public AIStatusResponse getStatus() {
        AIStatusResponse status = new AIStatusResponse();
        
        // 检查本地 Ollama
        boolean localOnline = checkOllamaOnline(ollamaBaseUrl);
        boolean remoteOnline = false;
        
        if (remoteEnabled && ollamaRemoteUrl != null && !ollamaRemoteUrl.isEmpty()) {
            remoteOnline = checkOllamaOnline(ollamaRemoteUrl);
        }
        
        if (localOnline) {
            status.setStatus("connected");
            status.setMessage("本地 Ollama 服务运行正常");
            status.setUrl(ollamaBaseUrl);
            currentOllamaUrl = ollamaBaseUrl;
        } else if (remoteOnline) {
            status.setStatus("connected");
            status.setMessage("本地 Ollama 离线，已切换到远程服务");
            status.setUrl(ollamaRemoteUrl);
            status.setHint("远程模式运行中");
            currentOllamaUrl = ollamaRemoteUrl;
        } else {
            status.setStatus("disconnected");
            status.setMessage("无法连接到 Ollama 服务");
            if (remoteEnabled && ollamaRemoteUrl != null && !ollamaRemoteUrl.isEmpty()) {
                status.setHint("请检查本地服务 (" + ollamaBaseUrl + ") 或配置远程服务 (" + ollamaRemoteUrl + ")");
            } else {
                status.setHint("请确保 Ollama 已安装并运行: ollama serve");
            }
            currentOllamaUrl = null;
        }
        
        status.setLocalOnline(localOnline);
        status.setRemoteOnline(remoteOnline);
        status.setRemoteEnabled(remoteEnabled);
        
        return status;
    }
}