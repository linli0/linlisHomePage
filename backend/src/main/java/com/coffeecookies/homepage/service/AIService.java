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
    
    private final WebClient.Builder webClientBuilder;
    
    public AIModelsResponse getModels() {
        try {
            WebClient webClient = webClientBuilder
                .baseUrl(ollamaBaseUrl)
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
        WebClient webClient = webClientBuilder
            .baseUrl(ollamaBaseUrl)
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
                    .doOnNext(outputStream::write)
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
        WebClient webClient = webClientBuilder
            .baseUrl(ollamaBaseUrl)
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
        try {
            WebClient webClient = webClientBuilder
                .baseUrl(ollamaBaseUrl)
                .build();
            
            webClient.get()
                .uri("/api/tags")
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(5));
            
            AIStatusResponse status = new AIStatusResponse();
            status.setStatus("connected");
            status.setMessage("Ollama 服务运行正常");
            return status;
            
        } catch (WebClientResponseException e) {
            log.warn("Ollama API returned error: {}", e.getStatusCode());
            AIStatusResponse status = new AIStatusResponse();
            status.setStatus("disconnected");
            status.setMessage("无法连接到 Ollama 服务");
            status.setHint("请确保 Ollama 已安装并运行: ollama serve");
            return status;
        } catch (Exception e) {
            log.warn("Failed to connect to Ollama service: {}", e.getMessage());
            AIStatusResponse status = new AIStatusResponse();
            status.setStatus("disconnected");
            status.setMessage("无法连接到 Ollama 服务");
            status.setHint("请确保 Ollama 已安装并运行: ollama serve");
            return status;
        }
    }
}