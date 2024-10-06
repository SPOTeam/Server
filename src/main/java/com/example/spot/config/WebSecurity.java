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

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final UserDetailsServiceCustom userDetailsService;
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {


        http.csrf( (csrf) -> csrf.disable());

        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers(new AntPathRequestMatcher("/api/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/sign-up/send-verification-code")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/sign-up/verify")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/reissue")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/login", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/login/kakao", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/members/sign-in/kakao", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/members/sign-in/kakao/redirect", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/member/test", "POST")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api-docs")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/**", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**", "GET")).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }

    private JwtAuthenticationFilter getJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, memberService, userDetailsService);
    }
}