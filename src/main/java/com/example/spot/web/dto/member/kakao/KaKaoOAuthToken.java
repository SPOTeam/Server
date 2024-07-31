package com.example.spot.web.dto.member.kakao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KaKaoOAuthToken {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KaKaoOAuthTokenDTO {
        private String token_type;
        private String access_token;
        private String refresh_token;
        private String expires_in;
        private String refresh_token_expires_in;
    }
}
