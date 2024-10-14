package com.example.spot.security.filters;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.auth.CustomUserDetails;
import com.example.spot.domain.auth.TempUserDetails;
import com.example.spot.service.member.MemberService;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.service.member.UserDetailsServiceCustom;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입 추가
    private final MemberService memberService; // MemberService 주입 추가
    private final UserDetailsServiceCustom userDetailsService; // UserDetailsServiceCustom 주입 추가

    /**
     * JWT 토큰을 검증하는 필터를 생성합니다.
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            // 임시 토큰 인증 요청 별도 처리
            if (isTempRequest(request)) {
                String tempToken = jwtTokenProvider.resolveToken(request); // 토큰 추출
                // 임시 토큰이 유효한 경우
                if (isValidToken(tempToken)) {
                    // 임시 토큰을 이용하여 사용자 인증
                    tempAuthenticateUser(tempToken);
                }
                // 필터 체인 진행
                filterChain.doFilter(request, response);
                return;
            }

            // 재발행 요청 별도 처리
            if (isReissueRequest(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            // 일반 인증 요청 처리
            String token = jwtTokenProvider.resolveToken(request);

            // 토큰이 유효한 경우 사용자 인증
            if (isValidToken(token))
                authenticateUser(token);

            filterChain.doFilter(request, response);
        } catch (GeneralException e) {
            handleException(response, e);
        }
    }


    private boolean isTempRequest(HttpServletRequest request) {
        return Objects.equals(request.getRequestURI(), "/spot/sign-up");
    }

    // 재발행 요청인지 확인
    private boolean isReissueRequest(HttpServletRequest request) {
        return Objects.equals(request.getRequestURI(), "/spot/reissue");
    }

    // 토큰 유효성 확인
    private boolean isValidToken(String token) {
        return token != null && jwtTokenProvider.validateToken(token);
    }

    // 임시 토큰을 이용하여 사용자 인증
    private void tempAuthenticateUser(String tempToken) {
        String email = jwtTokenProvider.getEmailByToken(tempToken);
        authenticate(email);
    }

    // 사용자 인증
    private void authenticateUser(String token) {
        Long memberId = jwtTokenProvider.getMemberIdByToken(token);
        authenticate(memberId.toString());
    }

    private void authenticate(String tempToken) {
        TempUserDetails userDetails = (TempUserDetails) userDetailsService.loadUserByUsername(tempToken);
        Authentication authentication = jwtTokenProvider.getAuthentication(tempToken, userDetails);
        log.info("Authenticated user: {}", userDetails.getUsername());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 예외 처리
    private void handleException(HttpServletResponse response, GeneralException e) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        try (PrintWriter writer = response.getWriter()) {
            // 예외 메시지 전송
            writer.write("Invalid JWT token: " + e.getStatus().getMessage());
            writer.flush();
        }
    }
}
