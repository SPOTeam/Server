package com.example.spot.repository.verification;

import com.example.spot.domain.auth.VerificationCode;
import com.example.spot.web.dto.token.TokenResponseDTO;

public interface VerificationCodeRepository {

    void addVerificationCode(String phone, String code);

    VerificationCode getVerificationCode(String phone);

    void setTempToken(TokenResponseDTO.TempTokenDTO tempTokenDTO, VerificationCode existingCode);
}
