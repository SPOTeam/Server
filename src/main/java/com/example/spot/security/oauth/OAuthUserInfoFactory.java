package com.example.spot.security.oauth;

import com.example.spot.web.dto.member.google.GoogleUserInfo;
import com.example.spot.web.dto.member.google.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class OAuthUserInfoFactory {

    public static OAuth2UserInfo getOAuthUserInfo(String provider, Map<String, Object> attributes) {

        if (provider.equals("google")) {

            log.info("------------------ GOOGLE 로그인 요청 ------------------");
            log.info("------------------ GOOGLE 로그인 요청 ------------------");

            return new GoogleUserInfo(attributes);
        }
        System.out.println("provider = " + provider);
        throw new RuntimeException("provider가 google이 아님");
    }
}
