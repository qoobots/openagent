package com.qoobot.agent.education_agent.security;

import com.qoobot.agent.education_agent.common.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具
 *
 * <p>采用 HS256 对称签名；密钥从配置文件注入。
 * 生产环境务必将密钥放入配置中心 / 环境变量，禁止硬编码。
 */
@Slf4j
@Component
public class JwtUtil {

    /** Claims Key */
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_STAGE = "stage";
    public static final String CLAIM_TYPE = "type";

    /** Token 类型 */
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${agent.security.jwt.secret:education-agent-default-jwt-secret-key-min-32-chars-2026}")
    private String secret;

    @Value("${agent.security.jwt.issuer:education-agent}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String role, String stage) {
        return generateToken(userId, role, stage, TYPE_ACCESS, Constants.ACCESS_TOKEN_EXPIRE);
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId, String role, String stage) {
        return generateToken(userId, role, stage, TYPE_REFRESH, Constants.REFRESH_TOKEN_EXPIRE);
    }

    private String generateToken(Long userId, String role, String stage, String type, long expireSeconds) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireSeconds * 1000L);
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_STAGE, stage);
        claims.put(CLAIM_TYPE, type);
        return Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expire)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析 Access Token
     *
     * @return 失败时返回 null
     */
    public LoginUser parseAccessToken(String token) {
        LoginUser user = parseToken(token);
        if (user == null) {
            return null;
        }
        return user;
    }

    /**
     * 解析 Refresh Token
     */
    public LoginUser parseRefreshToken(String token) {
        LoginUser user = parseToken(token);
        if (user == null) {
            return null;
        }
        return user;
    }

    private LoginUser parseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = claims.get(CLAIM_USER_ID, Number.class) == null
                    ? null : claims.get(CLAIM_USER_ID, Number.class).longValue();
            String role = claims.get(CLAIM_ROLE, String.class);
            String stage = claims.get(CLAIM_STAGE, String.class);
            if (userId == null) {
                return null;
            }
            return new LoginUser(userId, role, stage);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("Token 已过期: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Bearer 字符串中提取 Token
     */
    public String extractBearer(String header) {
        if (header == null || !header.startsWith(Constants.AUTH_PREFIX)) {
            return null;
        }
        return header.substring(Constants.AUTH_PREFIX.length()).trim();
    }
}
