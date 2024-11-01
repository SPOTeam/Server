package com.example.spot.web.dto.member.naver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class NaverCallback {

    private final String code;
    private final String state;
    private final String error;
    private final String errorDescription;

    @JsonCreator
    public NaverCallback(
            @JsonProperty("code") String code,
            @JsonProperty("state") String state,
            @JsonProperty("error") String error,
            @JsonProperty("error_description") String errorDescription) {
        this.code = code;
        this.state = state;
        this.error = error;
        this.errorDescription = errorDescription;
    }
}
