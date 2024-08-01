package com.example.spot.security.utils;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
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

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;
    @Value("${token.access_token_expiration_time}")
    private Long ACCESS_TOKEN_EXPIRATION_TIME;
    @Value("${token.refresh_token_expiration_time}")
    private Long REFRESH_TOKEN_EXPIRATION_TIME;

    @PostConstruct
    protected void init() {
        JWT_SECRET_KEY = Base64.getEncoder().encodeToString(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public TokenDTO createToken(Long memberId) {
        Date now = new Date();
        Date accessTokenExpirationTime = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);
        String accessToken = Jwts.builder()
            .claim("memberId", memberId)
            .setIssuedAt(now)
            .setExpiration(accessTokenExpirationTime)
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
            .compact();

        String refreshToken = Jwts.builder()
            .claim("memberId", memberId)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME))
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET_KEY)
            .compact();

        return TokenDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .accessTokenExpiresIn(ACCESS_TOKEN_EXPIRATION_TIME)
            .build();
    }

    public boolean isTokenExpired(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(JWT_SECRET_KEY).build().parseClaimsJws(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(JWT_SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token : {}", e.getMessage());
            throw new GeneralException(ErrorStatus._INVALID_JWT);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token : {}", e.getMessage());
            throw new GeneralException(ErrorStatus._EXPIRED_JWT);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token : {}", e.getMessage());
            throw new GeneralException(ErrorStatus._UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty. : {}", e.getMessage());
            throw new GeneralException(ErrorStatus._EMPTY_JWT);
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
    public String resolveRefreshToken(HttpServletRequest request) {
        return request.getHeader("refreshToken");
    }

    public String getUserPk(String token) {
        return Jwts.parser().setSigningKey(JWT_SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
    }

    public TokenDTO reissueToken(String refreshToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(JWT_SECRET_KEY).build().parseClaimsJws(refreshToken).getBody();
        Long memberId = claims.get("memberId", Long.class);

        return createToken(memberId);
    }

    public Long getMemberIdByToken(String token) {
        return Jwts.parserBuilder().setSigningKey(JWT_SECRET_KEY).build().parseClaimsJws(token).getBody().get("memberId", Long.class);
    }
}
