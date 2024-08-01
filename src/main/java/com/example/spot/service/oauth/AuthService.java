package com.example.spot.service.oauth;


import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.repository.MemberRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public TokenDTO reissueToken(String refreshToken) {
        if (jwtTokenProvider.isRefreshTokenExpired(refreshToken))
            throw new GeneralException(ErrorStatus._EXPIRED_JWT);

        return jwtTokenProvider.reissueToken(refreshToken);
    }

}
