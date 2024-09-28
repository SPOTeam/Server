package com.example.spot.domain.auth;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class VerificationCode {

    private final String phone;
    private String tempToken;
    private String code;
    private LocalDateTime expiredAt;

    @Builder
    public VerificationCode(String phone, String code, LocalDateTime expiredAt) {
        this.phone = phone;
        this.code = code;
        this.expiredAt = expiredAt;
    }

    public void resetVerificationCode(String code, LocalDateTime expiredAt) {
        this.code = code;
        this.expiredAt = expiredAt;
    }

    public void addTempToken(String tempToken) {
        this.tempToken = tempToken;
    }
}
