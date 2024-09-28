package com.example.spot.security.utils;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.web.dto.token.TokenResponseDTO;
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

    @Value("${token.access_secret}")
    private String JWT_SECRET_KEY;
    @Value("${token.access_token_expiration_time}")
    private Long ACCESS_TOKEN_EXPIRATION_TIME;
    @Value("${token.refresh_token_expiration_time}")
    private Long REFRESH_TOKEN_EXPIRATION_TIME;
    @Value("${token.temp_token_expiration_time}")
    private Long TEMP_TOKEN_EXPIRATION_TIME;

    @PostConstruct
    protected void init() {
        JWT_SECRET_KEY = Base64.getEncoder().encodeToString(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    // 액세스 및 리프레시 토큰 생성
    public TokenDTO createToken(Long memberId) {
        Date now = new Date();
        String accessToken = generateToken(memberId, now, ACCESS_TOKEN_EXPIRATION_TIME, "access");
        String refreshToken = generateToken(memberId, now, REFRESH_TOKEN_EXPIRATION_TIME, "refresh");

        return TokenDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .accessTokenExpiresIn(ACCESS_TOKEN_EXPIRATION_TIME)
            .build();
    }

    // 전화번호 인증을 위한 임시 토큰 생성
    public TokenResponseDTO.TempTokenDTO createTempToken(String phone) {
        Date now = new Date();
        String tempToken = generateTempToken(phone, now);

        return TokenResponseDTO.TempTokenDTO.builder()
                .tempToken(tempToken)
                .tempTokenExpiresIn(TEMP_TOKEN_EXPIRATION_TIME)
                .build();
    }

    // JWT 토큰 생성 -> 위 createToken 메서드에서 호출
    private String generateToken(Long memberId, Date now, long expirationTime, String tokenType) {
        return Jwts.builder()
            .claim("memberId", memberId)
            .claim("tokenType", tokenType)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + expirationTime))
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    // JWT 임시 토큰 생성 -> 위 createTempToken 메서드에서 호출
    private String generateTempToken(String phone, Date now) {
        return Jwts.builder()
                .claim("phone", phone)
                .claim("tokenType", "temp")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + TEMP_TOKEN_EXPIRATION_TIME))
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사 -> 유효기간 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return validateToken(token, true) == ErrorStatus._EXPIRED_JWT;
    }

    // 토큰 유효성 검사 -> 외부 호출 용
    public boolean validateToken(String token) {
        return validateToken(token, false) == null;
    }

    // 토큰 유효성 검사
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
        Claims claims = getClaims(token);
        return claims.get("memberId", Long.class);
    }

    public String getPhoneByToken(String tempToken) {
        Claims claims = getClaims(tempToken);
        return claims.get("phone", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
