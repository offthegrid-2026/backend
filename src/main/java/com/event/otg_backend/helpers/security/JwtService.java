package com.event.otg_backend.helpers.security;

import com.event.otg_backend.exceptions.auth.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecretKey signingKey;
    private final long expirationMs;

    private static final String REVOKED_KEY_PREFIX = "revoked:";

    public JwtService(
            RedisTemplate<String, String> redisTemplate,
            @Value("${spring.app.jwt.secret}") String secret,
            @Value("${spring.app.jwt.expiration-ms}") long expirationMs){
        this.redisTemplate = redisTemplate;
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

// GENERATE JWT TOKEN

    public String generateToken(Long userId, String email){
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public String generateAdminToken(String email){
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("email", email)
                .claim("role", "ADMIN")
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public boolean isAdmin(Claims claims){
        return "ADMIN".equals(claims.get("role", String.class));
    }

    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims validateAndGetClaims(String token){
        Claims claims;
        try {
            claims = parseClaims(token);
        }catch (ExpiredJwtException e){
            throw new InvalidTokenException("Session expired. Please log in again.");
        }catch (JwtException | IllegalArgumentException e){
            throw new InvalidTokenException("Invalid authentication token.");
        }

        if(isTokenRevoked(claims.getId())){
            throw new InvalidTokenException("This session has been logged out. Please log in again.");
        }

        return claims;
    }

    public Long extractUserId(Claims claims){
        return Long.valueOf(claims.getSubject());
    }

    public String extractEmail(Claims claims){
        return claims.get("email", String.class);
    }

    public boolean isTokenExpired(Claims claims){
        return claims.getExpiration().before(new Date());
    }

    public void revokeToken(String token){
        Claims claims;
        try {
            claims = parseClaims(token);
        }catch (ExpiredJwtException e){
            // Already expired — nothing left to revoke.
            return;
        }catch (JwtException | IllegalArgumentException e){
            throw new InvalidTokenException("Invalid authentication token.");
        }

        long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();

        if(remainingMs > 0){
            redisTemplate.opsForValue().set(
                    REVOKED_KEY_PREFIX + claims.getId(),
                    "true",
                    Duration.ofMillis(remainingMs)
            );
        }
    }

    private boolean isTokenRevoked(String jti){
        return Boolean.TRUE.equals(redisTemplate.hasKey(REVOKED_KEY_PREFIX + jti));
    }
}
