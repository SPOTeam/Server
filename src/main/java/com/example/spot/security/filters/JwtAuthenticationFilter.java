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

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService; // MemberService 주입 추가
    private final UserDetailsServiceCustom userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {

            // 임시 토큰 인증 요청 별도 처리
            if (isTempRequest(request)) {
                String tempToken = jwtTokenProvider.resolveToken(request);
                if (isValidToken(tempToken)) {
                    tempAuthenticateUser(tempToken);
                }
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

    private boolean isReissueRequest(HttpServletRequest request) {
        return Objects.equals(request.getRequestURI(), "/spot/reissue");
    }

    private boolean isValidToken(String token) {
        return token != null && jwtTokenProvider.validateToken(token);
    }

    private void tempAuthenticateUser(String tempToken) {
        String email = jwtTokenProvider.getEmailByToken(tempToken);
        authenticate(email);
    }

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

    private void handleException(HttpServletResponse response, GeneralException e) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        try (PrintWriter writer = response.getWriter()) {
            writer.write("Invalid JWT token: " + e.getStatus().getMessage());
            writer.flush();
        }
    }
}
