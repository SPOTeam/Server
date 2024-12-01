package com.example.spot.security.oauth;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.security.oauth.adpter.google.GoogleUserInfo;
import com.example.spot.security.oauth.adpter.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 추후 Spring Security OAuth2로 사용시 이곳에 구현하시면 됩니다.
 */

@Slf4j
public class OAuthUserInfoFactory {

    public static OAuth2UserInfo getOAuthUserInfo(String provider, Map<String, Object> attributes) {

        if (provider.equals("google")) {
            log.info("------------------ GOOGLE 로그인 요청 ------------------");
            return new GoogleUserInfo(attributes);
        }
        throw new MemberHandler(ErrorStatus._MEMBER_UNSUPPORTED_LOGIN_TYPE);

    }
}
