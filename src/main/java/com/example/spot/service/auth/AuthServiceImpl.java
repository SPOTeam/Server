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
import com.example.spot.web.dto.member.naver.NaverCallback;
import com.example.spot.web.dto.member.naver.NaverMember;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final NaverOAuthService naverOAuthService;

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

/* ----------------------------- 네이버 소셜로그인 API ------------------------------------- */

    @Override
    public void authorizeWithNaver(HttpServletRequest request, HttpServletResponse response) {
        String url = naverOAuthService.getNaverAuthorizeUrl();
        System.out.println(url);
        try {
            response.sendRedirect(url);
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus._NAVER_SIGN_IN_INTEGRATION_FAILED);
        }
    }

    @Override
    public MemberResponseDTO.NaverSignInDTO signInWithNaver(HttpServletRequest request, HttpServletResponse response, NaverCallback naverCallback) throws JsonProcessingException {

        NaverMember.ResponseDTO responseDTO = naverOAuthService.getNaverMember(request, response, naverCallback);
        String email = responseDTO.getResponse().getEmail();

        Boolean isSpotMember = Boolean.TRUE;

        // 가입되지 않은 회원이면 회원 정보 저장
        if (!memberRepository.existsByEmailAndLoginType(email, LoginType.NAVER)) {
            isSpotMember = Boolean.FALSE;
            signUpWithNaver(responseDTO);
        }

        Member member = memberRepository.findByEmailAndLoginType(email, LoginType.NAVER)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 로그인을 위한 토큰 발급
        TokenDTO token = jwtTokenProvider.createToken(member.getId());
        saveRefreshToken(member, token);

        MemberResponseDTO.MemberSignInDTO signInDTO = MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(token)
                .memberId(member.getId())
                .email(member.getEmail())
                .build();

        return MemberResponseDTO.NaverSignInDTO.toDTO(isSpotMember, signInDTO);
    }

    private void signUpWithNaver(NaverMember.ResponseDTO memberDTO) {
        String birthYear = memberDTO.getResponse().getBirthYear();
        String birthDay = memberDTO.getResponse().getBirthDay();

        LocalDate birth = null;
        if (birthYear != null && birthDay != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            birth = LocalDate.parse(birthYear + "-" + birthDay, formatter);
        }

        Gender gender;
        if (memberDTO.getResponse().getGender().equals("F")) {
            gender = Gender.FEMALE;
        } else if (memberDTO.getResponse().getGender().equals("M")) {
            gender = Gender.MALE;
        } else {
            gender = Gender.UNKNOWN;
        }

        Member member = Member.builder()
                .name(memberDTO.getResponse().getName())
                .nickname(memberDTO.getResponse().getNickname())
                .birth(birth)
                .gender(gender)
                .email(memberDTO.getResponse().getEmail())
                .carrier(Carrier.NONE)
                .phone("")
                .loginId(memberDTO.getResponse().getEmail())
                .password("")
                .profileImage(DEFAULT_PROFILE_IMAGE_URL)
                .personalInfo(false)
                .idInfo(false)
                .isAdmin(Boolean.FALSE)
                .loginType(LoginType.NAVER)
                .status(Status.ON)
                .build();

        memberRepository.save(member);
    }

/* ----------------------------- 일반 로그인/회원가입 API ------------------------------------- */

    /**
     * 일반 로그인을 위한 메서드입니다. 아이디와 비밀번호를 확인한 후 토큰을 발급하는 로직을 수행합니다.
     * @param signInDTO 로그인할 회원의 아이디와 비밀번호를 입력 받습니다.
     * @return 로그인한 회원의 토큰 정보(액세스 & 리프레시 토큰 & 만료기간), 이메일과 회원 아이디(정수)가 반환됩니다.
     */
    @Override
    public MemberResponseDTO.MemberSignInDTO signIn(MemberRequestDTO.SignInDTO signInDTO) {

        // 아이디가 일치하는 유저가 있는지 확인
        Member member = memberRepository.findByLoginId(signInDTO.getLoginId())
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

    /**
     * 인증 코드를 전송하는 메서드입니다.
     * 일반 회원가입, 아이디 찾기, 비밀번호 찾기에 공통으로 적용되는 인증 메일 전송 로직입니다.
     * @param request 클라이언트의 요청 정보 객체를 입력 받습니다.
     * @param response 서버의 응답 정보 객체를 입력 받습니다.
     * @param email 인증 코드를 전송할 이메일을 입력 받습니다.
     */
    @Override
    public void sendVerificationCode(HttpServletRequest request, HttpServletResponse response, String email) {

        // 인증 코드 생성
        String verificationCode = createCode();

        // 인증 코드 정보 저장
        verificationCodeRepository.addVerificationCode(email, verificationCode);

        // 인증 코드 전송
        mailService.sendMail(request, response, email, verificationCode);
    }

    /**
     * 인증 코드를 생성하는 메서드입니다.
     * @return 1 ~ 9999 사이의 랜덤한 정수를 반환합니다.
     */
    private String createCode() {
        Random random = new Random();
        int intCode = random.nextInt(10000); // 1 ~ 9999 사이의 정수
        return String.format("%04d", intCode);
    }

    /**
     * 이메일로 전송된 인증 코드를 메모리에 저장된 인증 코드 객체 정보와 비교 검증하는 메서드입니다.
     * 인증이 완료되면 일반 회원가입, 아이디 찾기, 비밀번호 찾기에 필요한 임시 토큰을 발급합니다.
     * @param code 이메일로 전달된 인증 코드를 입력 받습니다.
     * @param email 인증 코드가 전송된 이메일을 입력 받습니다.
     * @return 발급한 임시 토큰 정보(토큰 & 만료기간)를 반환합니다.
     */
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

    /**
     * 일반 회원가입에 사용되는 메서드입니다.
     * @param signUpDTO 회원의 기본 정보를 입력 받습니다.
     *                  name : 이름
     *                  nickname : 닉네임
     *                  frontRID : 주민번호 앞자리
     *                  backRID : 주민번호 뒷자리 첫 글자
     *                  email : 이메일
     *                  loginId : 아이디
     *                  password : 비밀번호
     *                  pwCheck : 비밀번호 확인
     *                  personalInfo : 개인정보활용 동의 여부
     *                  idInfo : 고유식별정보처리 동의 여부
     * @return 가입한 회원은 자동으로 로그인되며, 회원의 토큰 정보(액세스 & 리프레시 토큰 & 만료기간), 이메일과 회원 아이디(정수)가 반환됩니다.
     */
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

    /**
     * 생성된 리프레시 토큰을 DB에 저장하는 메서드입니다.
     * @param member 리프레시 토큰을 발급한 회원을 입력 받습니다.
     * @param token 저장할 토큰 정보(액세스 & 리프레시 토큰, 만료기간)를 입력 받습니다.
     */
    private void saveRefreshToken(Member member, TokenDTO token) {

        if (refreshTokenRepository.existsByMemberId(member.getId()))
            refreshTokenRepository.deleteAllByMemberId(member.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(member.getId())
                .token(token.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 아이디 찾기에 사용되는 메서드입니다. 임시 토큰을 검증한 후 이메일로 가입된 회원 정보를 확인합니다.
     * @return 아이디/이메일, 로그인 타입, 계정 생성일시가 반환합니다.
     */
    @Override
    public MemberResponseDTO.FindIdDTO findId() {

        // 임시 토큰 검증
        String email = SecurityUtils.getVerifiedTempUserEmail();

        // 이메일로 가입된 회원 정보 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        return MemberResponseDTO.FindIdDTO.toDTO(member);
    }

    /**
     * 비밀번호 찾기에 사용되는 메서드입니다. 임시 토큰을 검증한 후 아이디 & 이메일로 가입된 회원 정보를 확인합니다.
     * @param loginId 비밀번호를 찾고자 하는 회원의 아이디를 입력 받습니다.
     * @return 닉네임, 아이디, 발급된 임시 비밀번호를 반환합니다.
     */
    @Override
    public MemberResponseDTO.FindPwDTO findPw(String loginId) {

        // 임시 토큰 검증
        String email = SecurityUtils.getVerifiedTempUserEmail();

        // 이메일로 가입된 회원 정보 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 인증된 사용자의 아이디와 입력 아이디 일치 여부 확인
        if (!member.getLoginId().equals(loginId)) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);
        }

        // 임시 비밀번호 발급
        String tempPw = generateTempPassword();
        member.setPassword(tempPw);
        memberRepository.save(member);

        return MemberResponseDTO.FindPwDTO.toDTO(member);

    }

    @Override
    public MemberResponseDTO.AvailabilityDTO checkLoginIdAvailability(String loginId) {

        // 입력 조건 확인 (영어 대소문자 및 숫자 조합)
        String inputRegex = "(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]+";
        Pattern inputPattern = Pattern.compile(inputRegex);
        Matcher inputRegexMatcher = inputPattern.matcher(loginId);

        if (!inputRegexMatcher.matches()) {
            return MemberResponseDTO.AvailabilityDTO.toDTO(Boolean.FALSE, "NOT_CONTAIN_MIX_OF_LETTERS_AND_NUMBERS");
        }

        // 글자수 확인 (6자 이상)
        String lengthRegex = "[a-zA-Z0-9]{6,}";
        Pattern lengthPattern = Pattern.compile(lengthRegex);
        Matcher lengthRegexMatcher = lengthPattern.matcher(loginId);

        if (!lengthRegexMatcher.matches()) {
            return MemberResponseDTO.AvailabilityDTO.toDTO(Boolean.FALSE, "AT_LEAST_SIX_CHARACTERS_LONG");
        }

        // 기존 아이디와 중복 여부 확인
        if (memberRepository.existsByLoginId(loginId)) {
            return MemberResponseDTO.AvailabilityDTO.toDTO(Boolean.FALSE, "LOGIN_ID_ALREADY_EXISTS");
        }

        return MemberResponseDTO.AvailabilityDTO.toDTO(Boolean.TRUE, null);
    }

    @Override
    public MemberResponseDTO.AvailabilityDTO checkEmailAvailability(String email) {

        // 입력 조건 확인
        String inputRegex = "[^@+ ]+@[^@+ ]+";
        Pattern inputPattern = Pattern.compile(inputRegex);
        Matcher inputRegexMatcher = inputPattern.matcher(email);

        if (!inputRegexMatcher.matches()) {
            return MemberResponseDTO.AvailabilityDTO.toDTO(Boolean.FALSE, "NOT_MATCH_INPUT_CONDITION");
        }

        // 기존 이메일과 중복 여부 확인
        if (memberRepository.existsByEmail(email)) {
            return MemberResponseDTO.AvailabilityDTO.toDTO(Boolean.FALSE, "EMAIL_ALREADY_EXISTS");
        }

        return MemberResponseDTO.AvailabilityDTO.toDTO(Boolean.TRUE, null);
    }

    /**
     * 임시 비밀번호를 발급하는 메서드입니다.
     * 알파벳 대소문자, 숫자, 특수기호를 혼합하여 13자리 비밀번호를 생성합니다.
     * @return 생성된 임시 비밀번호를 반환합니다.
     */
    private String generateTempPassword() {

        // Char Set
        String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String LOWER = "abcdefghijklmnopqrstuvwxyz";
        String DIGITS = "0123456789";
        String SPECIALS = "!@#$%^&*()_+";

        String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIALS;

        SecureRandom random = new SecureRandom();
        return IntStream.range(0, 13)
                .map(i -> ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())))
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining());
    }

/* ----------------------------- 로그아웃 API ------------------------------------- */


}
