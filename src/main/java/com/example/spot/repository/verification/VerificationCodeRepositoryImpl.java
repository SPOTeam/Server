package com.example.spot.repository.verification;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.auth.VerificationCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class VerificationCodeRepositoryImpl implements VerificationCodeRepository {

    @Value("${token.temp_token_expiration_time}")
    private Long TEMP_TOKEN_EXPIRATION_TIME;

    private final List<VerificationCode> verificationCodes = new ArrayList<>();

    @Override
    public void addVerificationCode(String phone, String code) {
        if (phone != null && code != null) {

            // 만료기간이 지난 VerificationCode 삭제
            verificationCodes.removeIf(verificationCode -> verificationCode.getExpiredAt().isBefore(LocalDateTime.now()));

            // 동일한 임시 토큰으로 생성한 코드가 이미 존재하는지 확인
            VerificationCode existingCode = verificationCodes.stream()
                    .filter(verificationCode -> verificationCode.getPhone().equals(phone))
                    .findFirst()
                    .orElse(null);

            if (existingCode == null) {
                VerificationCode verificationCode = VerificationCode.builder()
                        .phone(phone)
                        .code(code)
                        .expiredAt(LocalDateTime.now().plus(Duration.ofMillis(TEMP_TOKEN_EXPIRATION_TIME)))
                        .build();
                this.verificationCodes.add(verificationCode);
            } else {
                existingCode.resetVerificationCode(code, LocalDateTime.now().plus(Duration.ofMillis(TEMP_TOKEN_EXPIRATION_TIME)));
            }

        }
    }

    @Override
    public String getVerificationCode(String phone) {

        VerificationCode code = verificationCodes.stream()
                .filter(vc -> vc.getPhone().equals(phone))
                .findFirst()
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_VERIFIED));

        return code.getCode();
    }

    @Override
    public String addTempToken(String tempToken) {
        return "";
    }
}
