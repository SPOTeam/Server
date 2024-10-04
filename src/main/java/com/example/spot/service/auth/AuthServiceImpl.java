package com.example.spot.service.auth;


import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.auth.RefreshToken;
import com.example.spot.domain.auth.VerificationCode;
import com.example.spot.domain.enums.LoginType;
import com.example.spot.domain.enums.Status;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.repository.verification.VerificationCodeRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.message.MessageService;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import io.sentry.protocol.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
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
    private final VerificationCodeRepository verificationCodeRepository;
    private final MessageService messageService;

    @Value("${image.post.anonymous.profile}")
    private String DEFAULT_PROFILE_IMAGE_URL;

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

    @Override
    public MemberResponseDTO.MemberSignInDTO signIn(MemberRequestDTO.SignInDTO signInDTO) {

        // 이메일이 일치하는 유저가 있는지 확인
        Member member = memberRepository.findByEmail(signInDTO.getEmail())
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 비밀번호 확인
        if (!signInDTO.getPassword().equals(member.getPassword())) {
            throw new MemberHandler(ErrorStatus._MEMBER_PASSWORD_NOT_MATCH);
        }

        // 토큰 발급
        TokenDTO tokenDTO = jwtTokenProvider.createToken(member.getId());
        saveRefreshToken(member, tokenDTO);

        return MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(tokenDTO)
                .memberId(member.getId())
                .email(member.getEmail())
                .build();
    }

    @Override
    public void sendVerificationCode(MemberRequestDTO.PhoneDTO phoneDTO) {

        // 이미 해당 전화번호로 가입된 회원이 존재하면 에러 반환
        if (memberRepository.existsByPhone(phoneDTO.getPhone())) {
            throw new MemberHandler(ErrorStatus._MEMBER_PHONE_ALREADY_EXISTS);
        }

        // 인증 코드 생성
        String verificationCode = createCode();

        // 인증 코드 정보 저장
        verificationCodeRepository.addVerificationCode(phoneDTO.getPhone(), verificationCode);

        // 인증 코드 전송
        messageService.sendMessage(phoneDTO.getPhone(), verificationCode);
    }

    private String createCode() {
        Random random = new Random();
        int intCode = random.nextInt(10000); // 1 ~ 9999 사이의 정수
        return String.format("%04d", intCode);
    }

    @Override
    public TokenResponseDTO.TempTokenDTO verifyPhone(String code, MemberRequestDTO.PhoneDTO phoneDTO) {

        // 인증 코드 확인
        VerificationCode verificationCode = verificationCodeRepository.getVerificationCode(phoneDTO.getPhone());
        if (!code.equals(verificationCode.getCode())) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_VERIFIED);
        }

        // 임시 토큰 생성
        TokenResponseDTO.TempTokenDTO tempTokenDTO = jwtTokenProvider.createTempToken(phoneDTO.getPhone());
        verificationCode.setTempToken(tempTokenDTO.getTempToken());

        // VerificationCode에 tempToken 정보 저장
        // addVerificationCode 호출 시 tempToken 만료 기간이 지난 VerificationCode 자동 삭제
        verificationCodeRepository.setTempToken(tempTokenDTO, verificationCode);

        return tempTokenDTO;
    }

    @Override
    public MemberResponseDTO.MemberSignInDTO signUp(MemberRequestDTO.SignUpDTO signUpDTO) {

        // 임시 토큰 검증
        String phone = SecurityUtils.getVerifiedTempUserPhone();

        // 회원 생성
        if (memberRepository.existsByEmail(signUpDTO.getEmail())) {
            throw new MemberHandler(ErrorStatus._MEMBER_EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByPhone(signUpDTO.getPhone())) {
            throw new MemberHandler(ErrorStatus._MEMBER_PHONE_ALREADY_EXISTS);
        }
        if (!signUpDTO.getIdInfo() || !signUpDTO.getPersonalInfo()) {
            throw new MemberHandler(ErrorStatus._TERMS_NOT_AGREED);
        }
        if (!signUpDTO.getPassword().equals(signUpDTO.getPwCheck())) {
            throw new MemberHandler(ErrorStatus._MEMBER_PW_AND_PW_CHECK_DO_NOT_MATCH);
        }
        if (!phone.equals(signUpDTO.getPhone())) {
            throw new MemberHandler(ErrorStatus._MEMBER_PHONE_NOT_VERIFIED);
        }

        Member member = Member.builder()
                .name(signUpDTO.getName())
                .password(signUpDTO.getPassword())
                .email(signUpDTO.getEmail())
                .carrier(signUpDTO.getCarrier())
                .phone(signUpDTO.getPhone())
                .birth(signUpDTO.getBirth())
                .profileImage(DEFAULT_PROFILE_IMAGE_URL)
                .personalInfo(signUpDTO.getPersonalInfo())
                .idInfo(signUpDTO.getIdInfo())
                .isAdmin(Boolean.FALSE)
                .loginType(LoginType.NORMAL)
                .status(Status.ON)
                .build();

        member = memberRepository.save(member);

        // JWT 토큰 생성
        TokenDTO token = jwtTokenProvider.createToken(member.getId());

        // JWT 리프레시 토큰 저장
        saveRefreshToken(member, token);

        return MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(token)
                .memberId(member.getId())
                .email(member.getEmail())
                .build();
    }

    private void saveRefreshToken(Member member, TokenDTO token) {

        if (refreshTokenRepository.existsByMemberId(member.getId()))
            refreshTokenRepository.deleteAllByMemberId(member.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(member.getId())
                .token(token.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

}
