package com.example.spot.web.dto.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TokenResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenDTO{
        private String accessToken;
        private String refreshToken;
        private Long accessTokenExpiresIn;
    }

}
