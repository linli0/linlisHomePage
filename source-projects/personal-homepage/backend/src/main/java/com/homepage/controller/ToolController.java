package com.homepage.controller;

import com.homepage.dto.CommandRequest;
import com.homepage.dto.CommandResponse;
import com.homepage.dto.Result;
import com.homepage.entity.CommandHistory;
import com.homepage.service.KimiService;
import com.homepage.service.ToolService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ToolController {

    private final ToolService toolService;
    private final KimiService kimiService;

    @PostMapping("/json/format")
    public Result<String> formatJson(@RequestBody Map<String, String> request) {
        String json = request.get("json");
        return Result.success(toolService.jsonFormat(json));
    }

    @PostMapping("/json/minify")
    public Result<String> minifyJson(@RequestBody Map<String, String> request) {
        String json = request.get("json");
        return Result.success(toolService.jsonMinify(json));
    }

    @PostMapping("/base64/encode")
    public Result<String> base64Encode(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return Result.success(toolService.base64Encode(text));
    }

    @PostMapping("/base64/decode")
    public Result<String> base64Decode(@RequestBody Map<String, String> request) {
        String encoded = request.get("encoded");
        return Result.success(toolService.base64Decode(encoded));
    }

    @PostMapping("/url/encode")
    public Result<String> urlEncode(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return Result.success(toolService.urlEncode(text));
    }

    @PostMapping("/url/decode")
    public Result<String> urlDecode(@RequestBody Map<String, String> request) {
        String encoded = request.get("encoded");
        return Result.success(toolService.urlDecode(encoded));
    }

    @PostMapping("/hash/md5")
    public Result<String> md5(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return Result.success(toolService.md5(text));
    }

    @PostMapping("/hash/sha1")
    public Result<String> sha1(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return Result.success(toolService.sha1(text));
    }

    @PostMapping("/hash/sha256")
    public Result<String> sha256(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return Result.success(toolService.sha256(text));
    }

    @PostMapping("/hash/sha512")
    public Result<String> sha512(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return Result.success(toolService.sha512(text));
    }

    @PostMapping("/timestamp/convert")
    public Result<ToolService.TimestampResult> timestampConvert(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        String fromFormat = request.getOrDefault("fromFormat", "timestamp_ms");
        return Result.success(toolService.timestampConvert(input, fromFormat));
    }

    @PostMapping("/qrcode/generate")
    public ResponseEntity<byte[]> generateQRCode(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        int width = (Integer) request.getOrDefault("width", 200);
        int height = (Integer) request.getOrDefault("height", 200);
        
        byte[] imageBytes = toolService.generateQRCode(content, width, height);
        
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header("Content-Disposition", "inline; filename=qrcode.png")
                .body(imageBytes);
    }

    @PostMapping("/kimi")
    public Result<CommandResponse> executeKimiCommand(
            @Valid @RequestBody CommandRequest request,
            HttpServletRequest httpRequest) {
        return Result.success(kimiService.executeKimiCommand(request, httpRequest));
    }

    @GetMapping("/kimi/history")
    public Result<List<CommandHistory>> getKimiHistory(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(kimiService.getCommandHistory(limit));
    }

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return Result.success(status);
    }
}
