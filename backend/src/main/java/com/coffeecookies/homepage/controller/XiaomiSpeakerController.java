package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.Result;
import com.coffeecookies.homepage.dto.XiaomiAiChatRequest;
import com.coffeecookies.homepage.dto.XiaomiResponse;
import com.coffeecookies.homepage.dto.XiaomiTtsRequest;
import com.coffeecookies.homepage.service.XiaomiSpeakerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/xiaomi")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "features.xiaomi", name = "enabled", havingValue = "true")
public class XiaomiSpeakerController {
    
    private final XiaomiSpeakerService xiaomiSpeakerService;
    
    /**
     * TTS 播报端点
     */
    @PostMapping("/tts")
    public ResponseEntity<Result<XiaomiResponse>> tts(@Valid @RequestBody XiaomiTtsRequest request) {
        try {
            XiaomiResponse response = xiaomiSpeakerService.ttsLocal(request.getText());
            return ResponseEntity.ok(Result.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("TTS 播报失败: " + e.getMessage()));
        }
    }
    
    /**
     * AI 对话端点
     */
    @PostMapping("/chat")
    public ResponseEntity<Result<XiaomiResponse>> chat(@Valid @RequestBody XiaomiAiChatRequest request) {
        try {
            XiaomiResponse response = xiaomiSpeakerService.chatWithAiLocal(request.getMessage());
            return ResponseEntity.ok(Result.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("AI 对话失败: " + e.getMessage()));
        }
    }
    
    /**
     * 唤醒小爱端点
     */
    @PostMapping("/wake")
    public ResponseEntity<Result<XiaomiResponse>> wake() {
        try {
            XiaomiResponse response = xiaomiSpeakerService.wakeLocal();
            return ResponseEntity.ok(Result.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("唤醒小爱失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取小爱音箱状态
     */
    @GetMapping("/status")
    public ResponseEntity<Result<XiaomiResponse>> getStatus() {
        try {
            boolean enabled = xiaomiSpeakerService.getXiaomiSpeakerConfig().isEnabled();
            String message = enabled ? "小爱音箱集成功能已启用" : "小爱音箱集成功能未启用";
            
            XiaomiResponse response = XiaomiResponse.builder()
                    .success(true)
                    .message(message)
                    .data(Map.of("enabled", enabled))
                    .build();
                    
            return ResponseEntity.ok(Result.success(response));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("获取状态失败: " + e.getMessage()));
        }
    }
}