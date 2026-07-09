package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.AIChatRequest;
import com.coffeecookies.homepage.dto.AIModelsResponse;
import com.coffeecookies.homepage.dto.AIStatusResponse;
import com.coffeecookies.homepage.dto.Result;
import com.coffeecookies.homepage.service.AIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {
    
    private final AIService aiService;
    
    @GetMapping("/models")
    public ResponseEntity<Result<AIModelsResponse>> getModels() {
        try {
            AIModelsResponse models = aiService.getModels();
            return ResponseEntity.ok(Result.success(models));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Result.error(503, 
                "请确保 Ollama 服务已启动 (" + e.getMessage() + ")"));
        }
    }
    
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody AIChatRequest request) {
        try {
            if (Boolean.TRUE.equals(request.getStream())) {
                // Streaming response
                StreamingResponseBody stream = aiService.chatStream(request);
                return ResponseEntity.ok()
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .header("Transfer-Encoding", "chunked")
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .body(stream);
            } else {
                // Non-streaming response - return JSON
                Map<String, Object> response = aiService.chatNonStream(request);
                return ResponseEntity.ok(Result.success(response));
            }
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Result.error(503,
                "Failed to communicate with Ollama service: " + e.getMessage()));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Result<AIStatusResponse>> getStatus() {
        try {
            AIStatusResponse status = aiService.getStatus();
            return ResponseEntity.ok(Result.success(status));
        } catch (Exception e) {
            AIStatusResponse status = new AIStatusResponse();
            status.setStatus("error");
            status.setMessage("检查 Ollama 状态时发生错误: " + e.getMessage());
            return ResponseEntity.ok(Result.success(status));
        }
    }
}