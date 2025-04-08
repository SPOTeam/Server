package com.example.spot.service.auth;


import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.auth.RsaKey;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.MemberThemeRepository;
import com.example.spot.repository.PreferredRegionRepository;
import com.example.spot.repository.StudyReasonRepository;
import com.example.spot.web.dto.member.MemberResponseDTO.CheckMemberDTO;
import com.example.spot.web.dto.rsa.Rsa;
import com.example.spot.domain.auth.RefreshToken;
import com.example.spot.domain.auth.VerificationCode;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.LoginType;
import com.example.spot.domain.enums.Status;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.repository.rsa.RSAKeyRepository;
import com.example.spot.repository.verification.VerificationCodeRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.security.utils.MemberUtils;
import com.example.spot.security.utils.RSAUtils;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.message.MailService;
import com.example.spot.web.dto.member.MemberResponseDTO.SocialLoginSignInDTO;
import com.example.spot.web.dto.member.naver.NaverCallback;
import com.example.spot.web.dto.member.naver.NaverMember;
import com.example.spot.web.dto.member.naver.NaverOAuthToken;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;

import java.security.PrivateKey;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    private final MemberThemeRepository memberThemeRepository;
    private final PreferredRegionRepository preferredRegionRepository;
    private final StudyReasonRepository studyReasonRepository;

    private final MailService mailService;
    private final NaverOAuthService naverOAuthService;

    private final RSAUtils rsaUtils;
    private final RSAKeyRepository rsaKeyRepository;

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

/* ----------------------------- 공통 회원 관리 API ------------------------------------- */

    @Override
    public MemberResponseDTO.MemberInfoCreationDTO signUpAndPartialUpdate(String nickname, Boolean personalInfo, Boolean idInfo) {

        // Authorization
        Long memberId = SecurityUtils.getCurrentUserId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        member.setNickname(nickname);
        member.updateTerm(personalInfo, idInfo);
        member = memberRepository.save(member);

        return MemberResponseDTO.MemberInfoCreationDTO.toDTO(member);
    }

    @Override
    public MemberResponseDTO.InactiveMemberDTO withdraw() {

        // Authorization
        Long memberId = SecurityUtils.getCurrentUserId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 운영중인 스터디가 있는 경우 탈퇴 불가
        if (memberStudyRepository.existsByMemberIdAndIsOwned(memberId, true)) {
            throw new MemberHandler(ErrorStatus._OWNED_STUDY_EXISTS);
        }

        // inactive 필드 활성화
        member.setInactive(LocalDateTime.now());

        // SecurityContextHolder 정리
        SecurityUtils.deleteCurrentUser();

        // Refresh Token 정리
        refreshTokenRepository.deleteAllByMemberId(memberId);

        return MemberResponseDTO.InactiveMemberDTO.toDTO(member);
    }

/* ----------------------------- 네이버 소셜로그인 API ------------------------------------- */

    /**
     * 네이버 로그인 인증 요청 URL로 실제 요청을 전송하고 로그인 페이지로 리디렉션하는 메서드입니다.
     * @param request : HTTPServletRequest
     * @param response : HttpServletResponse
     */
    @Override
    public void authorizeWithNaver(HttpServletRequest request, HttpServletResponse response) {
        String url = naverOAuthService.getNaverAuthorizeUrl();
        try {
            response.sendRedirect(url);
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus._NAVER_SIGN_IN_INTEGRATION_FAILED);
        }
    }

    /**
     * SPOT 서비스에 네이버를 통해 로그인과 회원가입을 수행하는 함수입니다.
     * 로그인 Callback 성공시 반환되는 naverCallback을 바탕으로 액세스 토큰을 발급받고 프로필에 접근합니다.
     * 현재 SPOT에 가입되지 않은 회원이라면, 반환된 프로필 정보를 기반으로 회원 정보를 생성하여 DB에 저장합니다.
     * 현재 SPOT에 가입되어 있는 회원이라면, 소셜로그인 후 토큰 정보를 반환합니다.
     * @param request : HttpServletRequest
     * @param response : HttpServletResponse
     * @param naverCallback : Callback 함수 성공시 반환되는 요소(code, state, error, error_description)
     * @return SocialLoginSignInDTO(isSpotMember, signInDTO-토큰정보)
     */
    @Override
    public SocialLoginSignInDTO signInWithNaver(HttpServletRequest request, HttpServletResponse response, NaverCallback naverCallback) throws Exception {
        NaverMember.ResponseDTO responseDTO = naverOAuthService.getNaverMember(request, response, naverCallback);
        return getSocialLoginSignInDTO(responseDTO);
    }

    /**
     * SPOT 서비스에 네이버를 통해 로그인과 회원가입을 수행하는 함수입니다.
     * 클라이언트로부터 전달받은 액세스 토큰을 통해 프로필에 접근합니다.
     * 현재 SPOT에 가입되지 않은 회원이라면, 반환된 프로필 정보를 기반으로 회원 정보를 생성하여 DB에 저장합니다.
     * 현재 SPOT에 가입되어 있는 회원이라면, 소셜로그인 후 토큰 정보를 반환합니다.
     * @param request : HttpServletRequest
     * @param response : HttpServletResponse
     * @return SocialLoginSignInDTO(isSpotMember, signInDTO-토큰정보)
     */
    @Override
    public SocialLoginSignInDTO signInWithNaver(HttpServletRequest request, HttpServletResponse response, NaverOAuthToken.NaverTokenIssuanceDTO naverTokenDTO) throws Exception {
        NaverMember.ResponseDTO responseDTO = naverOAuthService.getNaverMember(request, response, naverTokenDTO);
        return getSocialLoginSignInDTO(responseDTO);
    }

    /**
     * 네이버 회원 프로필을 통해 SocialLoginSignInDTO를 생성하는 함수입니다.
     * @param responseDTO : 네이버 회원 프로필 DTO
     * @return SocialLoginSignInDTO (SPOT 회원 정보 및 토큰 정보)
     */
    private SocialLoginSignInDTO getSocialLoginSignInDTO(NaverMember.ResponseDTO responseDTO) {
        String email = responseDTO.getResponse().getEmail();

        // 다른 로그인 방식을 사용한 계정이 있는지 확인
        if (memberRepository.existsByEmailAndLoginTypeNot(email, LoginType.NAVER)) {
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

            // 탈퇴한(inactive) 회원이면 기존 정보 삭제
            if (member.getInactive() != null) {
                refreshTokenRepository.deleteByMemberId(member.getId());
                memberRepository.deleteById(member.getId());
                entityManager.flush();
            }
            else {
                throw new MemberHandler(ErrorStatus._MEMBER_EMAIL_ALREADY_EXISTS);
            }
        }

        // 네이버 로그인 계정이 있는지 확인
        Boolean isSpotMember = Boolean.TRUE;
        if (memberRepository.existsByEmailAndLoginType(email, LoginType.NAVER)) {
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

            // 탈퇴한(inactive) 회원이면 기존 정보 삭제 후 회원 정보 저장
            if (member.getInactive() != null) {
                refreshTokenRepository.deleteByMemberId(member.getId());
                memberRepository.deleteById(member.getId());
                entityManager.flush();
                isSpotMember = Boolean.FALSE;
                signUpWithNaver(responseDTO);
            }
        }
        else {
            isSpotMember = Boolean.FALSE;
            signUpWithNaver(responseDTO);
        }

        Member member = memberRepository.findByEmailAndLoginType(email, LoginType.NAVER)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        if (!isMemberExistsByCheckList(member)) {
            isSpotMember = Boolean.FALSE;
        }

        // 로그인을 위한 토큰 발급
        TokenDTO token = jwtTokenProvider.createToken(member.getId());
        saveRefreshToken(member, token);

        MemberResponseDTO.MemberSignInDTO signInDTO = MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(token)
                .memberId(member.getId())
                .loginType(member.getLoginType())
                .email(member.getEmail())
                .build();

        return SocialLoginSignInDTO.toDTO(isSpotMember, signInDTO);
    }

    public boolean isMemberExistsByCheckList(Member member) {
        Long memberId = member.getId();
        return memberThemeRepository.existsByMemberId(memberId) &&
                preferredRegionRepository.existsByMemberId(memberId) &&
                studyReasonRepository.existsByMemberId(memberId);
    }

    /**
     * 현재 SPOT에 가입되어 있지 않은 회원에 한해 회원 정보를 생성하여 DB에 저장합니다.
     * @param memberDTO : naverCallback을 바탕으로 생성된 프로필 객체
     */
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
                .phone(MemberUtils.generatePhoneNumber())
                .loginId(memberDTO.getResponse().getEmail())
                .password("")
                .profileImage(memberDTO.getResponse().getProfileImage())
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
     * @param signInDTO   로그인할 회원의 아이디와 비밀번호를 입력 받습니다.
     * @return 로그인한 회원의 토큰 정보(액세스 & 리프레시 토큰 & 만료기간), 이메일과 회원 아이디(정수)가 반환됩니다.
     */
    @Override
    public MemberResponseDTO.MemberSignInDTO signIn(Long rsaId, MemberRequestDTO.SignInDTO signInDTO) throws Exception {

        // 아이디가 일치하는 유저가 있는지 확인
        Member member = memberRepository.findByLoginId(signInDTO.getLoginId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 탈퇴한 회원이면 로그인 불가
        if (member.getInactive() != null) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);
        }

        // 비밀번호 확인
        String password = getDecryptedPassword(rsaId, signInDTO.getPassword());
        if (!password.equals(member.getPassword())) {
            throw new MemberHandler(ErrorStatus._MEMBER_PASSWORD_NOT_MATCH);
        }

        // 토큰 발급
        TokenDTO tokenDTO = jwtTokenProvider.createToken(member.getId());
        saveRefreshToken(member, tokenDTO);

        return MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(tokenDTO)
                .memberId(member.getId())
                .loginType(member.getLoginType())
                .email(member.getEmail())
                .build();
    }

    private String getDecryptedPassword(Long rsaId, String encryptedPassword) throws Exception {

        // Private Key 추출 후 Session에 저장된 Private Key 초기화
        RsaKey rsaKey = rsaKeyRepository.findById(rsaId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._RSA_ERROR));
        rsaKeyRepository.deleteByCreatedAtBefore(LocalDateTime.now().minusMinutes(1));

        // 복호화된 패스워드 반환
        PrivateKey privateKey = rsaUtils.getPrivateKeyFromBase64String(rsaKey.getPrivateKey());
        return rsaUtils.getDecryptedText(privateKey, encryptedPassword);
    }

    @Override
    public Rsa.RSAPublicKey getRSAPublicKey() throws Exception {

        // RSA 생성
        RsaKey rsa = rsaUtils.createRSA();
        rsa = rsaKeyRepository.save(rsa);

        // Public Key 반환
        return Rsa.RSAPublicKey.builder()
                .rsaId(rsa.getId())
                .modulus(rsa.getModulus())
                .exponent(rsa.getExponent())
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
     *
     * @param rsaId
     * @param signUpDTO 회원의 기본 정보를 입력 받습니다.
     *                  name : 이름
     *                  frontRID : 주민번호 앞자리
     *                  backRID : 주민번호 뒷자리 첫 글자
     *                  email : 이메일
     *                  loginId : 아이디
     *                  password : 비밀번호 (RSA Key로 암호화한 값)
     *                  pwCheck : 비밀번호 확인
     * @return 가입한 회원은 자동으로 로그인되며, 회원의 토큰 정보(액세스 & 리프레시 토큰 & 만료기간), 이메일과 회원 아이디(정수)가 반환됩니다.
     */
    @Override
    public MemberResponseDTO.MemberSignInDTO signUp(Long rsaId, MemberRequestDTO.SignUpDTO signUpDTO) throws Exception {

        // 이미 존재하는 회원인 경우
        if (memberRepository.existsByEmail(signUpDTO.getEmail())) {

            Member member = memberRepository.findByEmail(signUpDTO.getEmail())
                    .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

            // 탈퇴한(inactive) 회원이면 기존 정보 삭제 후 회원 생성
            if (member.getInactive() != null) {
                refreshTokenRepository.deleteByMemberId(member.getId());
                memberRepository.deleteById(member.getId());
                entityManager.flush();
            }
            else {
                throw new MemberHandler(ErrorStatus._MEMBER_EMAIL_ALREADY_EXISTS);
            }
        }

        // 회원 생성
        String password = getDecryptedPassword(rsaId, signUpDTO.getPassword());
        String pwCheck = getDecryptedPassword(rsaId, signUpDTO.getPwCheck());
        if (!password.equals(pwCheck)) {
            throw new MemberHandler(ErrorStatus._MEMBER_PW_AND_PW_CHECK_DO_NOT_MATCH);
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
                .nickname(signUpDTO.getName())
                .birth(birth)
                .gender(gender)
                .email(signUpDTO.getEmail())
                .carrier(Carrier.NONE)
                .phone("")
                .loginId(signUpDTO.getLoginId())
                .password(password)
                .profileImage(DEFAULT_PROFILE_IMAGE_URL)
                .personalInfo(false)
                .idInfo(false)
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
                .loginType(member.getLoginType())
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

        // 탈퇴한 회원이면 아이디 찾기 불가
        if (member.getInactive() != null) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);
        }

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

        // 탈퇴한 회원이면 아이디 찾기 불가
        if (member.getInactive() != null) {
            throw new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND);
        }

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

    @Override
    public CheckMemberDTO checkIsSpotMember(Long loginId) {
        Member member = memberRepository.findById(loginId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        boolean memberExistsByCheckList = isMemberExistsByCheckList(member);

        return CheckMemberDTO.toDTO(memberExistsByCheckList);
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
