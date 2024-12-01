package com.example.spot.security.oauth.adpter;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.LoginType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth 로그인 성공 시 CustomOAuth2UserService에서 생성되는 객체입니다.
 * {@code CustomOAuth2UserService.loadUser()} 메서드의 반환 타입으로, {@link OAuth2User}를 구현하여 SecurityContext에 Principal을 등록하는 역할을 합니다.
 * Google 정보뿐만 아니라 애플리케이션의 회원 정보도 포함하여 SuccessHandler 등에서 Principal로 활용할 수 있도록 합니다.
 */

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final Member member;
    private final Map<String, Object> attributes;

    @Getter
    private final Boolean isSpotMember;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // Google로 로그인한 사용자를 기본 Role로 설정합니다.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getName() {
        if (member.getLoginType().equals(LoginType.GOOGLE)) {
            return attributes.get("name").toString();
        }

        return null;
    }

}
