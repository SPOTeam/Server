package com.example.spot.security.oauth.adpter.google;

import com.example.spot.security.oauth.adpter.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Spring Security 내부에서 "/oauth/authorize/google" 로그인에 성공한 후,
 * OAuth2LoginAuthenticationFilter가 가로채어 구글 API에 사용자 정보를 반환 받은 정보를 저장하는 객체
 *
 * Notion OAuth + Spring Security의 자동화 5 ~ 6 번 참고
**/


@RequiredArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public String getProfile() {
        return attributes.get("picture").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }

}
