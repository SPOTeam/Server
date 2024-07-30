package com.example.spot.utils.jwt;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final Long EXPIRATION_TIME = 1000L * 60 * 60 * 24;

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    @PostConstruct
    protected void init() {
        JWT_SECRET_KEY = Base64.getEncoder().encodeToString(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Claims body = Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(token).getBody();
            boolean isTokenExpired = body.getExpiration().before(new Date());
            if (isTokenExpired) {
                throw new GeneralException(ErrorStatus._EXPIRED_JWT);
            }
            return true;
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._INVALID_JWT);
        }
    }

    public Authentication getAuthentication(String token, UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더가 null이거나 "Bearer "로 시작하지 않는 경우 null 반환
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        // "Bearer " 접두사 제거 후 토큰 반환
        return authorization.substring("Bearer ".length()).trim();
    }

    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

}
