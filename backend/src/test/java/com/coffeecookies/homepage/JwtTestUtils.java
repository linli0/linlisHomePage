package com.coffeecookies.homepage;

import com.coffeecookies.homepage.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 测试工具类
 * 用于生成测试用的 JWT 令牌
 */
@Component
public class JwtTestUtils {

    private static final String TEST_JWT_SECRET = "test-secret-key-for-testing-purposes-only";
    private static final long TEST_JWT_EXPIRATION = 86400000; // 24 hours

    /**
     * 生成测试用 JWT 令牌
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEST_JWT_EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, TEST_JWT_SECRET)
                .compact();
    }

    /**
     * 生成过期的 JWT 令牌（用于测试过期场景）
     */
    public static String generateExpiredToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - TEST_JWT_EXPIRATION - 1000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS512, TEST_JWT_SECRET)
                .compact();
    }

    /**
     * 验证 JWT 令牌（使用测试密钥）
     */
    public static Claims validateToken(String token) {
        return Jwts.parser()
                .setSigningKey(TEST_JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}