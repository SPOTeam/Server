package com.example.spot.service.oauth;


import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import java.util.List;
import java.util.Objects;
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
        // 리프레시 토큰 만료 여부 확인
        if (jwtTokenProvider.isRefreshTokenExpired(refreshToken))
            throw new GeneralException(ErrorStatus._EXPIRED_JWT);

        // 리프레시 토큰에서 memberId 추출
        Long memberIdByToken = jwtTokenProvider.getMemberIdByToken(refreshToken);

        // memberId로 회원 조회
        Member member = memberRepository.findById(memberIdByToken)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 회원의 리프레시 토큰과 요청된 리프레시 토큰 비교
        if (!Objects.equals(member.getRefreshToken(), refreshToken))
            throw new GeneralException(ErrorStatus._INVALID_JWT);

        // 토큰 재발급
        return jwtTokenProvider.reissueToken(refreshToken);
    }

}
