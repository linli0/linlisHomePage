package com.coffeecookies.homepage;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 测试工具类
 * 用于生成测试用的 JWT 令牌
 */
@Component
public class JwtTestUtils {

    private static final SecretKey TEST_JWT_SECRET = Keys.hmacShaKeyFor(
            "test-secret-key-for-testing-purposes-only-32bytes".getBytes(StandardCharsets.UTF_8)
    );
    private static final long TEST_JWT_EXPIRATION = 86400000; // 24 hours

    /**
     * 生成测试用 JWT 令牌
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEST_JWT_EXPIRATION))
                .signWith(TEST_JWT_SECRET)
                .compact();
    }

    /**
     * 生成过期的 JWT 令牌（用于测试过期场景）
     */
    public static String generateExpiredToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - TEST_JWT_EXPIRATION - 1000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(TEST_JWT_SECRET)
                .compact();
    }

    /**
     * 验证 JWT 令牌（使用测试密钥）
     */
    public static Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(TEST_JWT_SECRET)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
