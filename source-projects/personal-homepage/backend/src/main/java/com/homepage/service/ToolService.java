package com.homepage.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public String jsonFormat(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object obj = mapper.readValue(json, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON: " + e.getMessage());
        }
    }

    public String jsonMinify(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Object obj = mapper.readValue(json, Object.class);
            return mapper.writeValueAsString(obj);
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
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Base64 string");
        }
    }

    public String urlEncode(String text) {
        return java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    public String urlDecode(String encoded) {
        return java.net.URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }

    public String md5(String text) {
        return hash(text, "MD5");
    }

    public String sha1(String text) {
        return hash(text, "SHA-1");
    }

    public String sha256(String text) {
        return hash(text, "SHA-256");
    }

    public String sha512(String text) {
        return hash(text, "SHA-512");
    }

    private String hash(String text, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available: " + algorithm);
        }
    }

    public TimestampResult timestampConvert(String input, String fromFormat) {
        TimestampResult result = new TimestampResult();
        
        try {
            long timestamp;
            if ("timestamp_ms".equals(fromFormat)) {
                timestamp = Long.parseLong(input);
                result.setTimestampMillis(timestamp);
                result.setTimestampSeconds(timestamp / 1000);
            } else if ("timestamp_s".equals(fromFormat)) {
                timestamp = Long.parseLong(input) * 1000;
                result.setTimestampMillis(timestamp);
                result.setTimestampSeconds(Long.parseLong(input));
            } else if ("iso".equals(fromFormat)) {
                LocalDateTime dateTime = LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME);
                timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                result.setTimestampMillis(timestamp);
                result.setTimestampSeconds(timestamp / 1000);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
                timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                result.setTimestampMillis(timestamp);
                result.setTimestampSeconds(timestamp / 1000);
            }

            Instant instant = Instant.ofEpochMilli(result.getTimestampMillis());
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            
            result.setLocalTime(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.setUtcTime(dateTime.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(java.time.ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.setIsoTime(dateTime.format(DateTimeFormatter.ISO_DATE_TIME));

        } catch (Exception e) {
            throw new RuntimeException("Invalid input format: " + e.getMessage());
        }
        
        return result;
    }

    public byte[] generateQRCode(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);
            
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }

    public static class TimestampResult {
        private long timestampMillis;
        private long timestampSeconds;
        private String localTime;
        private String utcTime;
        private String isoTime;

        public long getTimestampMillis() { return timestampMillis; }
        public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }
        public long getTimestampSeconds() { return timestampSeconds; }
        public void setTimestampSeconds(long timestampSeconds) { this.timestampSeconds = timestampSeconds; }
        public String getLocalTime() { return localTime; }
        public void setLocalTime(String localTime) { this.localTime = localTime; }
        public String getUtcTime() { return utcTime; }
        public void setUtcTime(String utcTime) { this.utcTime = utcTime; }
        public String getIsoTime() { return isoTime; }
        public void setIsoTime(String isoTime) { this.isoTime = isoTime; }
    }
}
