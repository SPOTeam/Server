package com.example.spot.config;


import com.example.spot.security.filters.JwtAuthenticationFilter;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.service.member.MemberService;
import com.example.spot.service.member.UserDetailsServiceCustom;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {

    // JWT 토큰을 생성하고 유효성을 검사하는 JwtTokenProvider
    private final JwtTokenProvider jwtTokenProvider;
    // 회원 정보를 처리하는 MemberService
    private final MemberService memberService;
    // 사용자 정보를 처리하는 UserDetailsServiceCustom
    private final UserDetailsServiceCustom userDetailsService;

    /**
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

        // CSRF 보안 설정을 비활성화합니다.
        http.csrf( (csrf) -> csrf.disable());

        // HttpSecurity 설정을 구성합니다. JWT 토큰을 통한 검증을 거치지 않는 요청은 permitAll()로 설정합니다.
        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers(new AntPathRequestMatcher("/api/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/check/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/send-verification-code")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/verify")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/reissue")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/login", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/members/sign-in/naver/redirect", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/members/sign-in/naver/authorize", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/login/kakao", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/members/sign-in/kakao", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/members/sign-in/kakao/redirect", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/member/test", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api-docs")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**", "GET")).permitAll()
                .anyRequest().authenticated()
            )
            // JWT 토큰을 검증하는 필터를 UsernamePasswordAuthenticationFilter 앞에 추가합니다.
            .addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // 세션을 사용하지 않도록 설정합니다.
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // X-Frame-Options를 설정합니다.
        http.headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }

    /**
     * JWT 토큰을 검증하는 필터를 생성합니다.
     * @return JwtAuthenticationFilter
     */
    private JwtAuthenticationFilter getJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, memberService, userDetailsService);
    }
}