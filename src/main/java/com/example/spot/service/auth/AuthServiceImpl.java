package com.example.spot.service.auth;


import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.auth.RefreshToken;
import com.example.spot.domain.auth.VerificationCode;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.LoginType;
import com.example.spot.domain.enums.Status;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.repository.verification.VerificationCodeRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.message.MailService;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService{

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final MailService mailService;

    @Value("${image.post.anonymous.profile}")
    private String DEFAULT_PROFILE_IMAGE_URL;

    /**
     * 토큰을 재발급 합니다.
     * @param refreshToken 리프레시 토큰
     * @return 새로운 토큰을 생성하여 반환합니다.
     * @throws GeneralException 토큰이 만료되었거나, 잘못된 토큰일 경우 발생합니다.
     */
    @Override
    public TokenDTO reissueToken(String refreshToken) {

        // 리프레시 토큰 조회 및 검증
        RefreshToken tokenInDB = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN));

        // 리프레시 토큰 만료 확인
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

        // 토큰 재발급
        TokenDTO tokenDTO = jwtTokenProvider.reissueToken(refreshToken);

        // 리프레시 토큰 저장
        RefreshToken token = RefreshToken.builder()
            .memberId(member.getId())
            .token(tokenDTO.getRefreshToken())
            .build();

        // 기존 리프레시 토큰 삭제
        if (refreshTokenRepository.existsByMemberId(member.getId()))
            refreshTokenRepository.deleteByMemberId(member.getId());

        // 새로운 리프레시 토큰 저장
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
    public void sendVerificationCode(HttpServletRequest request, HttpServletResponse response, String email) {

        // 이미 해당 전화번호로 가입된 회원이 존재하면 에러 반환
        if (memberRepository.existsByEmail(email)) {
            throw new MemberHandler(ErrorStatus._MEMBER_EMAIL_ALREADY_EXISTS);
        }

        // 인증 코드 생성
        String verificationCode = createCode();

        // 인증 코드 정보 저장
        verificationCodeRepository.addVerificationCode(email, verificationCode);

        // 인증 코드 전송
        mailService.sendMail(request, response, email, verificationCode);
    }

    private String createCode() {
        Random random = new Random();
        int intCode = random.nextInt(10000); // 1 ~ 9999 사이의 정수
        return String.format("%04d", intCode);
    }

    @Override
    public TokenResponseDTO.TempTokenDTO verifyEmail(String code, String email) {

        // 인증 코드 확인
        VerificationCode verificationCode = verificationCodeRepository.getVerificationCode(email);
        if (!code.equals(verificationCode.getCode())) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_VERIFIED);
        }

        // 임시 토큰 생성
        TokenResponseDTO.TempTokenDTO tempTokenDTO = jwtTokenProvider.createTempToken(email);
        verificationCode.setTempToken(tempTokenDTO.getTempToken());

        // VerificationCode에 tempToken 정보 저장
        // addVerificationCode 호출 시 tempToken 만료 기간이 지난 VerificationCode 자동 삭제
        verificationCodeRepository.setTempToken(tempTokenDTO, verificationCode);

        return tempTokenDTO;
    }

    @Override
    public MemberResponseDTO.MemberSignInDTO signUp(MemberRequestDTO.SignUpDTO signUpDTO) {

        // 임시 토큰 검증
        String email = SecurityUtils.getVerifiedTempUserEmail();

        // 회원 생성
        if (memberRepository.existsByEmail(signUpDTO.getEmail())) {
            throw new MemberHandler(ErrorStatus._MEMBER_EMAIL_ALREADY_EXISTS);
        }
        if (!signUpDTO.getIdInfo() || !signUpDTO.getPersonalInfo()) {
            throw new MemberHandler(ErrorStatus._TERMS_NOT_AGREED);
        }
        if (!signUpDTO.getPassword().equals(signUpDTO.getPwCheck())) {
            throw new MemberHandler(ErrorStatus._MEMBER_PW_AND_PW_CHECK_DO_NOT_MATCH);
        }
        if (!email.equals(signUpDTO.getEmail())) {
            throw new MemberHandler(ErrorStatus._MEMBER_EMAIL_NOT_VERIFIED);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        LocalDate birth = LocalDate.parse(signUpDTO.getFrontRID(), formatter);

        Gender gender;
        if (signUpDTO.getBackRID().equals("1") || signUpDTO.getBackRID().equals("3")) {
            gender = Gender.MALE;
        } else if (signUpDTO.getBackRID().equals("2") || signUpDTO.getBackRID().equals("4")) {
            gender = Gender.FEMALE;
        } else {
            gender = Gender.UNKNOWN;
        }

        Member member = Member.builder()
                .name(signUpDTO.getName())
                .nickname(signUpDTO.getNickname())
                .birth(birth)
                .gender(gender)
                .email(signUpDTO.getEmail())
                .carrier(Carrier.NONE)
                .phone("")
                .loginId(signUpDTO.getLoginId())
                .password(signUpDTO.getPassword())
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
