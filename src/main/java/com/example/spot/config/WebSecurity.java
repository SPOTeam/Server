package com.example.spot.config;


import com.example.spot.config.jwt.JwtAuthenticationFilter;
import com.example.spot.config.jwt.JwtTokenProvider;
import com.example.spot.service.member.MemberService;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {


        http.csrf( (csrf) -> csrf.disable());


        http.authorizeHttpRequests((authz) -> authz
                .requestMatchers(new AntPathRequestMatcher("/login/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/login", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/spot/members/sign-in/kakao", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/swagger-config", "GET")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**", "GET")).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }

    private JwtAuthenticationFilter getJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, memberService);
    }
}