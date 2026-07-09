package com.coffeecookies.homepage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ToolService {

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final ObjectMapper minifyMapper = new ObjectMapper();

    public String jsonFormat(String json) {
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(jsonObject);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON: " + e.getMessage());
        }
    }

    public String jsonMinify(String json) {
        try {
            Object jsonObject = minifyMapper.readValue(json, Object.class);
            return minifyMapper.writeValueAsString(jsonObject);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON: " + e.getMessage());
        }
    }

    public String base64Encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    public String base64Decode(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Invalid Base64: " + e.getMessage());
        }
    }

    public String urlEncode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    public String urlDecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Invalid URL encoding: " + e.getMessage());
        }
    }

    public String md5(String text) {
        return DigestUtils.md5Hex(text);
    }

    public String sha1(String text) {
        return DigestUtils.sha1Hex(text);
    }

    public String sha256(String text) {
        return DigestUtils.sha256Hex(text);
    }

    public String sha512(String text) {
        return DigestUtils.sha512Hex(text);
    }

    public TimestampResult timestampConvert(String input, String fromFormat) {
        try {
            long timestamp;
            
            switch (fromFormat) {
                case "timestamp_ms" -> timestamp = Long.parseLong(input);
                case "timestamp_s" -> timestamp = Long.parseLong(input) * 1000;
                case "iso" -> {
                    LocalDateTime dateTime = LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME);
                    timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                }
                default -> throw new RuntimeException("Unknown format: " + fromFormat);
            }
            
            Instant instant = Instant.ofEpochMilli(timestamp);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            
            return TimestampResult.builder()
                    .timestampMs(timestamp)
                    .timestampSec(timestamp / 1000)
                    .iso(dateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                    .formatted(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Conversion error: " + e.getMessage());
        }
    }

    public byte[] generateQRCode(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("QR Code generation error: " + e.getMessage());
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimestampResult {
        private long timestampMs;
        private long timestampSec;
        private String iso;
        private String formatted;
    }
}
