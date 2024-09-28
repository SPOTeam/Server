package com.example.spot.domain.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class VerificationCode {

    private final String phone;

    @Setter
    private String code;

    @Setter
    private String tempToken;

    @Setter
    private LocalDateTime expiredAt;

    @Builder
    public VerificationCode(String phone, String code, LocalDateTime expiredAt) {
        this.phone = phone;
        this.code = code;
    }
}
