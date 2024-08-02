package com.example.spot.service.auth;


import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.Member;
import com.example.spot.domain.auth.RefreshToken;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public TokenDTO reissueToken(String refreshToken) {
        // 리프레시 토큰 추출
        log.info("refreshToken: {}", refreshToken);

        // 리프레시 토큰 조회 및 검증
        RefreshToken tokenInDB = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN));

        if (jwtTokenProvider.isTokenExpired(tokenInDB.getToken())) {
            refreshTokenRepository.delete(tokenInDB);
            throw new GeneralException(ErrorStatus._EXPIRED_REFRESH_TOKEN);
        }

        // 리프레시 토큰에서 memberId 추출
        Long memberIdByToken = jwtTokenProvider.getMemberIdByToken(refreshToken);

        // memberId로 회원 조회
        Member member = memberRepository.findById(memberIdByToken)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 회원의 리프레시 토큰과 요청된 리프레시 토큰 비교
        if (!Objects.equals(member.getId(), memberIdByToken))
            throw new GeneralException(ErrorStatus._INVALID_JWT);

        TokenDTO tokenDTO = jwtTokenProvider.reissueToken(refreshToken);

        RefreshToken token = RefreshToken.builder()
            .memberId(member.getId())
            .token(tokenDTO.getRefreshToken())
            .build();

        if (refreshTokenRepository.existsByMemberId(member.getId()))
            refreshTokenRepository.deleteByMemberId(member.getId());

        refreshTokenRepository.save(token);

        // 토큰 재발급
        return tokenDTO;
    }

}
