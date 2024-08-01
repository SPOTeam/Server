package com.example.spot.security.filters;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.service.member.MemberService;
import com.example.spot.security.utils.JwtTokenProvider;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            if (isReissueRequest(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = jwtTokenProvider.resolveToken(request);
            if (isValidToken(token))
                authenticateUser(token);
            filterChain.doFilter(request, response);
        } catch (GeneralException e) {
            handleException(response, e);
        }
    }

    private boolean isReissueRequest(HttpServletRequest request) {
        return Objects.equals(request.getRequestURI(), "/spot/reissue");
    }

    private boolean isValidToken(String token) {
        return token != null && jwtTokenProvider.validateToken(token);
    }

    private void authenticateUser(String token) {
        Long memberId = jwtTokenProvider.getMemberIdByToken(token);
        UserDetails userDetails = memberService.loadUserByUsername(memberId.toString());
        Authentication authentication = jwtTokenProvider.getAuthentication(token, userDetails);
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
