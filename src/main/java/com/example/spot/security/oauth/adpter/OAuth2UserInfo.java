package com.example.spot.security.oauth.adpter;

/**
 * 추후 Spring Security OAuth2로 사용시 해당 interface를 구현하시면 됩니다.
 */

public interface OAuth2UserInfo {
    String getProfile();

    String getEmail();

    String getName();

}
