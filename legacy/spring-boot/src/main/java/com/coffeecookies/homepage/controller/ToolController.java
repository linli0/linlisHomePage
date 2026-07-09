package com.coffeecookies.homepage.controller;

import com.coffeecookies.homepage.dto.Result;
import com.coffeecookies.homepage.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ToolController {

    private final ToolService toolService;

    @PostMapping("/json/format")
    public ResponseEntity<Result<String>> formatJson(@RequestBody Map<String, String> request) {
        String json = request.get("json");
        return ResponseEntity.ok(Result.success(toolService.jsonFormat(json)));
    }

    @PostMapping("/json/minify")
    public ResponseEntity<Result<String>> minifyJson(@RequestBody Map<String, String> request) {
        String json = request.get("json");
        return ResponseEntity.ok(Result.success(toolService.jsonMinify(json)));
    }

    @PostMapping("/base64/encode")
    public ResponseEntity<Result<String>> base64Encode(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(Result.success(toolService.base64Encode(text)));
    }

    @PostMapping("/base64/decode")
    public ResponseEntity<Result<String>> base64Decode(@RequestBody Map<String, String> request) {
        String encoded = request.get("encoded");
        return ResponseEntity.ok(Result.success(toolService.base64Decode(encoded)));
    }

    @PostMapping("/url/encode")
    public ResponseEntity<Result<String>> urlEncode(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(Result.success(toolService.urlEncode(text)));
    }

    @PostMapping("/url/decode")
    public ResponseEntity<Result<String>> urlDecode(@RequestBody Map<String, String> request) {
        String encoded = request.get("encoded");
        return ResponseEntity.ok(Result.success(toolService.urlDecode(encoded)));
    }

    @PostMapping("/hash/md5")
    public ResponseEntity<Result<String>> md5(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(Result.success(toolService.md5(text)));
    }

    @PostMapping("/hash/sha1")
    public ResponseEntity<Result<String>> sha1(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(Result.success(toolService.sha1(text)));
    }

    @PostMapping("/hash/sha256")
    public ResponseEntity<Result<String>> sha256(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(Result.success(toolService.sha256(text)));
    }

    @PostMapping("/hash/sha512")
    public ResponseEntity<Result<String>> sha512(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(Result.success(toolService.sha512(text)));
    }

    @PostMapping("/timestamp/convert")
    public ResponseEntity<Result<ToolService.TimestampResult>> timestampConvert(@RequestBody Map<String, String> request) {
        String input = request.get("input");
        String fromFormat = request.getOrDefault("fromFormat", "timestamp_ms");
        return ResponseEntity.ok(Result.success(toolService.timestampConvert(input, fromFormat)));
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

    @GetMapping("/health")
    public ResponseEntity<Result<Map<String, Object>>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(Result.success(status));
    }
}
