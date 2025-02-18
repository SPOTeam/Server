package com.example.spot.web.dto.member.naver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class NaverOAuthToken {

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverTokenIssuanceDTO {

        @Schema(name = "access_token", description = "Access token")
        private final String accessToken;

        @Schema(name = "refresh_token", description = "Refresh token")
        private final String refreshToken;

        @Schema(name = "token_type", description = "Token type")
        private final String tokenType;

        @Schema(name = "expires_in", description = "Expiration time in seconds")
        private final Integer expiresIn;

        @Schema(name = "error", description = "Error code")
        private final String error;

        @Schema(name = "error_description", description = "Error description")
        private final String errorDescription;

        @JsonCreator
        public NaverTokenIssuanceDTO(
                @JsonProperty("access_token") String accessToken,
                @JsonProperty("refresh_token") String refreshToken,
                @JsonProperty("token_type") String tokenType,
                @JsonProperty("expires_in") Integer expiresIn,
                @JsonProperty("error") String error,
                @JsonProperty("error_description") String errorDescription) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.error = error;
            this.errorDescription = errorDescription;
        }
    }

    @Getter
    public static class NaverTokenRenewalDTO {

        private final String accessToken;
        private final String tokenType;
        private final Integer expiresIn;
        private final String error;
        private final String errorDescription;

        public NaverTokenRenewalDTO(
                @JsonProperty("access_token") String accessToken,
                @JsonProperty("token_type") String tokenType,
                @JsonProperty("expires_in") Integer expiresIn,
                @JsonProperty("error") String error,
                @JsonProperty("error_description") String errorDescription) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.error = error;
            this.errorDescription = errorDescription;
        }

    }

    @Getter
    public static class NaverTokenDeleteDTO {

        private final String accessToken;
        private final String result;
        private final Integer expiresIn;
        private final String error;
        private final String errorDescription;

        public NaverTokenDeleteDTO(
                @JsonProperty("access_token") String accessToken,
                @JsonProperty("result") String result,
                @JsonProperty("expires_in") Integer expiresIn,
                @JsonProperty("error") String error,
                @JsonProperty("error_description") String errorDescription) {
            this.accessToken = accessToken;
            this.result = result;
            this.expiresIn = expiresIn;
            this.error = error;
            this.errorDescription = errorDescription;
        }
    }

}
