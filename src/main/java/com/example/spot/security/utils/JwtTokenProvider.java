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

    /**
     * 토큰을 생성합니다.
     * @param memberId 회원 ID
     * @return 생성된 토큰
     */
    // 액세스 및 리프레시 토큰 생성
    public TokenDTO createToken(Long memberId) {
        // 현재 시간
        Date now = new Date();
        String accessToken = generateToken(memberId, now, ACCESS_TOKEN_EXPIRATION_TIME, "access"); // 액세스 토큰 생성
        String refreshToken = generateToken(memberId, now, REFRESH_TOKEN_EXPIRATION_TIME, "refresh"); // 리프레시 토큰 생성

        // 토큰 DTO 반환
        return TokenDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .accessTokenExpiresIn(ACCESS_TOKEN_EXPIRATION_TIME)
            .build();
    }

    //

    /**
     * 전화번호 인증을 위한 임시 토큰을 생성합니다
     * @param email 이메일
     * @return  생성된 임시 토큰
     */
    public TokenResponseDTO.TempTokenDTO createTempToken(String email) {
        Date now = new Date();
        // 임시 토큰 생성
        String tempToken = generateTempToken(email, now);

        return TokenResponseDTO.TempTokenDTO.builder()
                .tempToken(tempToken)
                .tempTokenExpiresIn(TEMP_TOKEN_EXPIRATION_TIME)
                .build();
    }

    /**
     * JWT 토큰 생성 -> 위 createToken 메서드에서 호출
     * @param memberId 회원 ID
     * @param now 현재 시간
     * @param expirationTime 만료 시간
     * @param tokenType 토큰 타입
     * @return 생성된 토큰
     */
    private String generateToken(Long memberId, Date now, long expirationTime, String tokenType) {
        return Jwts.builder()
            .claim("memberId", memberId) // 회원 ID
            .claim("tokenType", tokenType) // 토큰 타입
            .setIssuedAt(now) // 발급 시간
            .setExpiration(new Date(now.getTime() + expirationTime)) // 만료 시간
            .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
            .compact();
    }

    // JWT 임시 토큰 생성 -> 위 createTempToken 메서드에서 호출

    /**
     * JWT 임시 토큰 생성
     * @param email 이메일
     * @param now  현재 시간
     * @return 생성된 임시 토큰
     */
    private String generateTempToken(String email, Date now) {
        return Jwts.builder()
                .claim("email", email)
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
            // 만료된 토큰
            if (checkExpirationOnly) return ErrorStatus._EXPIRED_JWT;
            throw new GeneralException(ErrorStatus._EXPIRED_JWT);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // 잘못된 JWT 서명
            logInvalidToken(e);
            throw new GeneralException(ErrorStatus._INVALID_JWT);
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT
            logInvalidToken(e);
            throw new GeneralException(ErrorStatus._UNSUPPORTED_JWT);
        } catch (IllegalArgumentException e) {
            // JWT 문자열이 비어 있음
            logInvalidToken(e);
            throw new GeneralException(ErrorStatus._EMPTY_JWT);
        }
    }

    private void logInvalidToken(Exception e) {
        log.info("Invalid JWT Token : {}", e.getMessage());
    }

    /**
     * 토큰을 이용하여 사용자 인증을 수행합니다.
     * @param token 토큰
     * @param userDetails 사용자 정보
     * @return 사용자 인증 정보
     */
    public Authentication getAuthentication(String token, UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * 토큰을 해석하여 회원 ID를 반환합니다.
     * @param request HTTP 요청
     * @return 회원 ID
     */
    public String resolveToken(HttpServletRequest request) {
        return resolveHeaderToken(request, "Authorization", "Bearer ");
    }

    /**
     * 리프레시 토큰을 해석하여 반환합니다.
     * @param request HTTP 요청
     * @return 리프레시 토큰
     */
    public String resolveRefreshToken(HttpServletRequest request) {
        return resolveHeaderToken(request, "refreshToken", "");
    }

    /**
     * 헤더에서 토큰을 추출합니다.
     * @param request HTTP 요청
     * @param headerName 헤더 이름
     * @param prefix 토큰 접두사
     * @return 추출된 토큰
     */
    private String resolveHeaderToken(HttpServletRequest request, String headerName, String prefix) {
        String headerValue = request.getHeader(headerName);
        if (headerValue == null || !headerValue.startsWith(prefix)) {
            return null;
        }
        return headerValue.substring(prefix.length()).trim();
    }

    /**
     * 리프레시 토큰을 이용하여 토큰을 재발급합니다.
     * @param refreshToken 리프레시 토큰
     * @return 새로 발급된 토큰
     */
    public TokenDTO reissueToken(String refreshToken) {
        Long memberId = getMemberIdByToken(refreshToken);
        return createToken(memberId);
    }

    /**
     * 토큰을 이용하여 회원 ID를 반환합니다.
     * @param token 토큰
     * @return 회원 ID
     */
    public Long getMemberIdByToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("memberId", Long.class);
    }

    /**
     * 임시 토큰을 이용하여 이메일을 반환합니다.
     * @param tempToken 임시 토큰
     * @return 이메일
     */
    public String getEmailByToken(String tempToken) {
        Claims claims = getClaims(tempToken);
        return claims.get("email", String.class);
    }

    /**
     * 토큰을 해석하여 클레임을 반환합니다.
     * @param token 토큰
     * @return 클레임
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
