package com.example.admin.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和验证 JWT Token
 */
@Component
public class JwtUtil {
    
    // JWT 密钥（生产环境应该从配置文件读取）
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Token 有效期：1小时
    private static final long EXPIRATION_TIME = 3600000;
    
    /**
     * 生成 JWT Token
     * @param userId 用户ID（SIP URI）
     * @param displayName 用户显示名
     * @return JWT Token
     */
    public String generateToken(String userId, String displayName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("displayName", displayName);
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }
    
    /**
     * 从 Token 中提取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
    
    /**
     * 从 Token 中提取用户显示名
     * @param token JWT Token
     * @return 用户显示名
     */
    public String getDisplayNameFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("displayName");
    }
    
    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @return true 如果有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 解析 Token
     * @param token JWT Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 获取 Token 过期时间（秒）
     * @return 过期时间（秒）
     */
    public long getExpirationSeconds() {
        return EXPIRATION_TIME / 1000;
    }
}
