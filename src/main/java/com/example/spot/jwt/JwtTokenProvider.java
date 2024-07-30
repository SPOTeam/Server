package com.example.spot.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String MEMBER_ID = "memberId";
    private static final Long EXPIRATION_TIME = 1000L * 60 * 60 * 24;

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    @Value("${jwt.expiration}")
    private Long JWT_EXPIRATION_TIME;

    @PostConstruct
    protected void init() {
        JWT_SECRET_KEY = Base64.getEncoder().encodeToString(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims getClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(Authentication authentication) {

        final Date now = new Date();

        final Claims claims = Jwts.claims()
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + EXPIRATION_TIME));

        claims.put(MEMBER_ID, authentication.getPrincipal());

        return Jwts.builder()
        .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(getSigningKey())
                .compact();
    }

    public JwtValidationType validateToken(String token) {
        try {
            getClaims(token);
            return JwtValidationType.VALID_JWT;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return JwtValidationType.INVALID_JWT_TOKEN;
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return JwtValidationType.EXPIRED_JWT_TOKEN;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return JwtValidationType.EMPTY_JWT_TOKEN;
        }
    }

    private Claims getBody(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getMemberFromJwt(String token) {
        Claims claims = getBody(token);
        return Long.valueOf(claims.get(MEMBER_ID).toString());

    }

    public boolean isTokenExpired(String token) {
        // 토큰 만료 여부 확인
        //TODO: 메서드 구현
        return false;
    }
}
