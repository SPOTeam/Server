package com.example.spot.service.auth;

import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;

public interface AuthService {

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
    TokenDTO reissueToken(String refreshToken);

}
