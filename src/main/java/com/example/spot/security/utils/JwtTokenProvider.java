package com.example.spot.security.utils;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
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
        String accessToken = generateToken(memberId, now, ACCESS_TOKEN_EXPIRATION_TIME);
        String refreshToken = generateToken(memberId, now, REFRESH_TOKEN_EXPIRATION_TIME);

        return TokenDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .accessTokenExpiresIn(ACCESS_TOKEN_EXPIRATION_TIME)
            .build();
    }

    private String generateToken(Long memberId, Date now, long expirationTime) {
        return Jwts.builder()
            .claim("memberId", memberId)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + expirationTime))
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean isTokenExpired(String token) {
        return validateToken(token, true) == ErrorStatus._EXPIRED_JWT;
    }

    public boolean validateToken(String token) {
        return validateToken(token, false) == null;
    }

    private ErrorStatus validateToken(String token, boolean checkExpirationOnly) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes())).build().parseClaimsJws(token);
            return null;
        } catch (ExpiredJwtException e) {
            if (checkExpirationOnly) return ErrorStatus._EXPIRED_JWT;
            throw new GeneralException(ErrorStatus._EXPIRED_JWT);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logInvalidToken(e);
            throw new GeneralException(ErrorStatus._INVALID_JWT);
        } catch (UnsupportedJwtException e) {
            logInvalidToken(e);
            throw new GeneralException(ErrorStatus._UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            logInvalidToken(e);
            throw new GeneralException(ErrorStatus._EMPTY_JWT);
        }
    }

    private void logInvalidToken(Exception e) {
        log.info("Invalid JWT Token : {}", e.getMessage());
    }

    public Authentication getAuthentication(String token, UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        return resolveHeaderToken(request, "Authorization", "Bearer ");
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return resolveHeaderToken(request, "refreshToken", "");
    }

    private String resolveHeaderToken(HttpServletRequest request, String headerName, String prefix) {
        String headerValue = request.getHeader(headerName);
        if (headerValue == null || !headerValue.startsWith(prefix)) {
            return null;
        }
        return headerValue.substring(prefix.length()).trim();
    }

    public TokenDTO reissueToken(String refreshToken) {
        Long memberId = getMemberIdByToken(refreshToken);
        return createToken(memberId);
    }

    public Long getMemberIdByToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.get("memberId", Long.class);
    }
}
