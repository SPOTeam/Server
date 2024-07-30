package com.example.spot.config.jwt;

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
                log.error("[validateToken] 토큰이 만료되었습니다.");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[validateToken] 유효하지 않은 토큰입니다.");
            return false;
        }
    }

    public Authentication getAuthentication(String token, UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        return authorization.substring("Bearer ".length());
    }

    // 토큰에서 회원 정보 추출
    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

}
