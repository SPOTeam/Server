package com.example.spot.web.dto.token;

import lombok.*;

public class TokenResponseDTO {

    @Getter
    @RequiredArgsConstructor
    @Builder
    public static class TempTokenDTO {
        private final String tempToken;
        private final Long tempTokenExpiresIn;
    }

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
