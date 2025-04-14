package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.member.MemberResponseDTO.NicknameDuplicateDTO;
import com.example.spot.web.dto.rsa.Rsa;
import com.example.spot.service.auth.AuthService;
import com.example.spot.validation.annotation.TextLength;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.SocialLoginSignInDTO;
import com.example.spot.web.dto.member.naver.NaverCallback;
import com.example.spot.web.dto.member.naver.NaverOAuthToken;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Tag(name = "회원 관리 API", description = "회원 관리 API")
    @Operation(summary = "[세션 유지] 액세스 토큰 재발급 API",
        description = """
            ## [세션 유지] 액세스 토큰을 재발급 하는 API입니다.
            리프레시 토큰을 통해 액세스 토큰을 재발급 합니다.
            리프레시 토큰의 만료 기간 이전인 경우에만 재발급이 가능합니다.
            액세스 토큰을 재발급 하는 경우, 리프레시 토큰도 재발급 됩니다.
            """)
    @PostMapping("/reissue")
    public ApiResponse<TokenDTO> reissueToken(HttpServletRequest request,
        @RequestHeader("refreshToken") String refreshToken){
        return ApiResponse.onSuccess(SuccessStatus._CREATED, authService.reissueToken(refreshToken));
    }

/* ----------------------------- 공통 회원 관리 API ------------------------------------- */

    // 닉네임 중복 확인
    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[공통 회원 관리] 닉네임 중복 확인 API",
            description = """
            ## [공통 회원 관리] 닉네임 중복 확인 API입니다.
            * Request Params : String nickname
            * Response Body : Boolean isAvailable
            
            중복된 이메일이 존재하면 duplicate 필드가 true로 설정됩니다. 
            """)
    @GetMapping("/check/nickname")
    public ApiResponse<NicknameDuplicateDTO> checkNicknameAvailability(
            @RequestParam @TextLength(max = 8) String nickname) {
        NicknameDuplicateDTO nicknameDuplicateDTO = authService.checkNicknameAvailability(nickname);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_NICKNAME_CHECK_COMPLETED, nicknameDuplicateDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[공통 회원 관리] 닉네임 생성 및 약관 동의 API",
            description = """
            ## [공통 회원 관리] 소셜 회원가입 혹은 일반 회원가입 이후 닉네임 및 약관 동의사항을 업데이트하는 API입니다.
            * Authorization 헤더에 액세스 토큰을 포함해야 합니다.
            * Request Params : String nickname, Boolean personalInfo, Boolean idInfo
            * Response Body : Long memberId, LocalDateTime updatedAt
            """)
    @PostMapping("/sign-up/update")
    public ApiResponse<MemberResponseDTO.MemberInfoCreationDTO> signUpAndPartialUpdate(
            @RequestBody MemberRequestDTO.SignUpDetailDTO signUpDetailDTO) {
        MemberResponseDTO.MemberInfoCreationDTO memberInfoCreationDTO = authService.signUpAndPartialUpdate(signUpDetailDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_UPDATED, memberInfoCreationDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[공통 회원 관리] 회원탈퇴 API",
            description = """
            ## [공통 회원 관리] 로그인한 회원이 SPOT을 탈퇴할 때 사용되는 API입니다.
            * Authorization 헤더에 액세스 토큰을 포함해야 합니다.
            * 회원탈퇴 시 해당 회원의 inactive(LocalDateTime) 필드가 활성화 됩니다.
            * 회원 정보 및 회원의 스터디 정보는 30일간 DB에 저장되며 30일이 지나면 자동으로 삭제됩니다.
            * 30일 이후 정보 삭제 시 "로그인 이메일", "성명", "생년월일 정보", "진행중 스터디", \
            "모집중 스터디", "스터디 찜 정보", "게시글", "댓글", "사진", "관심사", "관심지역"이  삭제됩니다.
            """)
    @PatchMapping("/withdraw")
    public ApiResponse<MemberResponseDTO.InactiveMemberDTO> withdraw() {
        MemberResponseDTO.InactiveMemberDTO inactiveMemberDTO = authService.withdraw();
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_DELETED, inactiveMemberDTO);
    }

/* ----------------------------- 네이버 소셜로그인 API ------------------------------------- */

    @Tag(name = "테스트 용 API", description = "테스트 용 API")
    @Operation(summary = "!서버용! [네이버 로그인] 테스트용 인증코드 발급 API",
            description = """
            ## [네이버 로그인] 네이버 액세스 토큰 발급에 필요한 인증코드를 발급 받는 API입니다.
            * API를 호출하면 네이버 로그인 페이지로 리디렉션됩니다.
            * 로그인을 완료하면 <네이버 회원 조회> 콜백 함수가 실행됩니다.
            * 회원가입이 되어있는 경우 -> 로그인 & 토큰 정보 반환
            * 회원가입이 되어있지 않은 경우 -> 회원가입 & 로그인 & 토큰 정보 반환
            ** 테스트 시 API URL을 복사하여 웹 브라우저에서 실행해주세요!!**
            """)
    @GetMapping("/members/sign-in/naver/authorize/test")
    public void authorizeWithNaver(HttpServletRequest request, HttpServletResponse response) {
        authService.authorizeWithNaver(request, response);
    }

    @Tag(name = "테스트 용 API", description = "테스트 용 API")
    @Operation(summary = "!서버용! [네이버 로그인] 테스트용 네이버 로그인/회원가입 API",
            description = """
            ## [네이버 로그인] 네이버 액세스 토큰을 발급하여 로그인/회원가입을 수행하는 콜백 함수입니다
            ### (직접 호출하는 API가 아닙니다)
            * 회원가입이 되어있는 경우 -> 로그인 & 토큰 정보 반환
            * 회원가입이 되어있지 않은 경우 -> 회원가입 & 로그인 & 토큰 정보 반환
            * 콜백 함수의 결과로 토큰 정보가 반환됩니다.
            """)
    @GetMapping("/members/sign-in/naver/redirect")
    public ApiResponse<SocialLoginSignInDTO> signInWithNaver(
            HttpServletRequest request, HttpServletResponse response, NaverCallback naverCallback) throws Exception {
        SocialLoginSignInDTO socialLoginSignInDTO = authService.signInWithNaver(request, response, naverCallback);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_SIGNED_IN, socialLoginSignInDTO);
    }

    @Tag(name = "네이버 로그인 API - 개발 완료", description = "네이버 로그인 API")
    @Operation(summary = "[네이버 로그인] 네이버 로그인/회원가입 API",
            description = """
            ## [네이버 로그인] 클라이언트로부터 네이버 액세스 토큰을 받아 로그인/회원가입을 수행하는 함수입니다
            * 회원가입이 되어있는 경우 -> 로그인 & 토큰 정보 반환
            * 회원가입이 되어있지 않은 경우 -> 회원가입 & 로그인 & 토큰 정보 반환
            """)
    @PostMapping(value = "/members/sign-in/naver", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<SocialLoginSignInDTO> signInWithNaver(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody NaverOAuthToken.NaverTokenIssuanceDTO naverTokenDTO
    ) throws Exception {
        SocialLoginSignInDTO socialLoginSignInDTO = authService.signInWithNaver(request, response, naverTokenDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_SIGNED_IN, socialLoginSignInDTO);
    }

/* ----------------------------- 일반 로그인/회원가입 API ------------------------------------- */

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[인증메일] 인증번호 전송 API",
            description = """
            ## [인증메일] 인증번호 전송 API입니다.
            * 입력받은 이메일로 인증번호가 전송됩니다.
            """)
    @PostMapping("/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(
            HttpServletRequest request, HttpServletResponse response,
            @RequestParam String email) {
        authService.sendVerificationCode(request, response, email);
        return ApiResponse.onSuccess(SuccessStatus._VERIFICATION_EMAIL_SENT);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[인증메일] 이메일 인증 API",
            description = """
            ## [인증메일] 이메일로 전송된 인증코드를 확인하는 API입니다.
            * 사용자로부터 인증코드와 이메일을 받아 검증 작업을 수행한 후, 임시 토큰을 반환합니다.
            * 임시 토큰은 최대 5분간 유효합니다. 임시 토큰이 만료된 경우 이메일 재인증이 필요합니다.
            """)
    @PostMapping("/verify")
    public ApiResponse<TokenResponseDTO.TempTokenDTO> verifyEmail(
            @RequestParam String verificationCode,
            @RequestParam String email) {
        TokenResponseDTO.TempTokenDTO tempTokenDTO = authService.verifyEmail(verificationCode, email);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_EMAIL_VERIFIED, tempTokenDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[회원 가입] 아이디 사용 가능 여부 확인 API",
            description = """
            ## [회원 가입] 아이디 사용 가능 여부 확인 API입니다.
            * 로그인 아이디를 입력 받아 아이디의 사용 가능 여부를 반환합니다.
            """)
    @GetMapping("/check/login-id")
    public ApiResponse<MemberResponseDTO.AvailabilityDTO> checkLoginIdAvailability(
            @RequestParam String loginId) {
        MemberResponseDTO.AvailabilityDTO availabilityDTO = authService.checkLoginIdAvailability(loginId);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_LOGIN_ID_CHECK_COMPLETED, availabilityDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[회원 가입] 이메일 사용 가능 여부 확인 API",
            description = """
            ## [회원 가입] 이메일 사용 가능 여부 확인 API입니다.
            * 이메일을 입력 받아 이메일의 사용 가능 여부를 반환합니다.
            """)
    @GetMapping("/check/email")
    public ApiResponse<MemberResponseDTO.AvailabilityDTO> checkEmailAvailability(
            @RequestParam String email) {
        MemberResponseDTO.AvailabilityDTO availabilityDTO = authService.checkEmailAvailability(email);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_EMAIL_CHECK_COMPLETED, availabilityDTO);
    }


    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[회원 가입] 일반 회원 가입 API",
        description = """
            ## [회원 가입] 일반 회원 가입 API입니다.
            * 아이디(이메일)과 비밀번호 등을 포함하여 회원 가입을 진행합니다.
            * 주민번호 앞자리(frontRID)와 뒷자리(backRID)는 모두 String 타입입니다.
            * <비밀번호>와 <비밀번호 확인>은 반드시 RSA Public Key로 암호화하여 전송해야 합니다.
            * 회원 가입에 성공하면, 액세스 토큰과 리프레시 토큰이 발급됩니다.
            * 액세스 토큰은 사용자의 정보를 인증하는데 사용되며, 리프레시 토큰은 액세스 토큰이 만료된 경우, 액세스 토큰을 재발급 하는데 사용됩니다.
            * 액세스 토큰이 만료된 경우, 유효한 상태의 리프레시 토큰을 통해 액세스 토큰을 재발급 받을 수 있습니다.
            """)
    @PostMapping("/sign-up")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> signUp(
            @RequestParam Long rsaId,
            @RequestBody @Valid MemberRequestDTO.SignUpDTO signUpDTO) throws Exception {
        MemberResponseDTO.MemberSignInDTO memberSignUpDTO = authService.signUp(rsaId, signUpDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, memberSignUpDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[아이디 찾기] 아이디 찾기 API",
            description = """
            ## [아이디 찾기] 이메일 인증을 통해 계정 정보를 불러옵니다.
            * 인증번호 전송 API > 이메일 인증 API 호출 후 해당 API를 호출해야 합니다.
            * 이메일 인증 API로부터 발급 받은 임시 토큰이 Authorization 헤더에 포함되어야 합니다.
            * 임시 토큰을 발급 받은 이메일로 가입된 계정 정보를 반환합니다.
            """)
    @PostMapping("/find-id")
    public ApiResponse<MemberResponseDTO.FindIdDTO> findId() {
        MemberResponseDTO.FindIdDTO findIdDTO = authService.findId();
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_LOGIN_ID_FOUND, findIdDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[비밀번호 찾기] 비밀번호 찾기 API",
            description = """
            ## [비밀번호 찾기] 이메일 인증 & 아이디 확인을 통해 임시 비밀번호를 발급합니다.
            * 인증번호 전송 API > 이메일 인증 API 호출 후 해당 API를 호출해야 합니다.
            * 이메일 인증 API로부터 발급 받은 임시 토큰이 Authorization 헤더에 포함되어야 합니다.
            * 이메일과 아이디 정보를 통해 비밀번호를 발급하여 반환합니다.
            """)
    @PostMapping("/find-pw")
    public ApiResponse<MemberResponseDTO.FindPwDTO> findPw(
            @RequestParam String loginId) {
        MemberResponseDTO.FindPwDTO findPwDTO = authService.findPw(loginId);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_FOUND, findPwDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[로그인] RSA Public Key 발급 API",
            description = """
            ## [로그인] 비밀번호 전송을 위해 RSA Public Key를 발급하는 API입니다.
            * 서버에서 발급한 RSA Public Key와 해당 키의 식별자인 rsaId를 클라이언트에 전달합니다.
            * 로그인 및 회원가입 시 해당 키를 통해 비밀번호를 암호화하여 전송해야 합니다.
            """)
    @PostMapping("/login/rsa")
    public ApiResponse<Rsa.RSAPublicKey> getRSAPublicKey() throws Exception {
        Rsa.RSAPublicKey rsaPublicKey = authService.getRSAPublicKey();
        return ApiResponse.onSuccess(SuccessStatus._RSA_PUBLIC_KEY_FOUND, rsaPublicKey);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[로그인] 일반 로그인 API",
        description = """
            ## [로그인] 아이디(이메일)과 비밀번호를 통해 로그인 하는 API입니다.
            * 로그인에 성공하면, 액세스 토큰과 리프레시 토큰이 발급됩니다.
            * 액세스 토큰은 사용자의 정보를 인증하는데 사용되며, 리프레시 토큰은 액세스 토큰이 만료된 경우, 액세스 토큰을 재발급 하는데 사용됩니다.
            * 액세스 토큰이 만료된 경우, 유효한 상태의 리프레시 토큰을 통해 액세스 토큰을 재발급 받을 수 있습니다.
            * rsaId에는 RSA Public Key 발급 API를 통해 획득한 식별자(id)를 입력해야 합니다.
            * 비밀번호는 반드시 RSA Public Key로 암호화하여 전송해야 합니다.
            """)
    @PostMapping("/login")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> login(
            @RequestParam Long rsaId,
            @RequestBody @Valid MemberRequestDTO.SignInDTO signInDTO) throws Exception {
        MemberResponseDTO.MemberSignInDTO memberSignInDTO = authService.signIn(rsaId, signInDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_SIGNED_IN, memberSignInDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[가입 유무 확인] 서비스 가입 유무 확인 API",
            description = """
            ## [가입 유무 확인] 서비스 가입 유무 확인 API입니다.
            해당 유저가 가입 후 체크리스트를 작성 했는지 확인합니다.
            * 가입 후 체크리스트를 작성한 경우 true, 작성하지 않은 경우 false를 반환합니다.
            """)
    @GetMapping("/check")
    public ApiResponse<MemberResponseDTO.CheckMemberDTO> checkIsSpotMember() {
        MemberResponseDTO.CheckMemberDTO checkMemberDTO = authService.checkIsSpotMember(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_SIGNUP_CHECK_COMPLETED, checkMemberDTO);
    }

/* ----------------------------- 로그아웃 API ------------------------------------- */

    @Tag(name = "회원 관리 API - 개발 중", description = "회원 관리 API")
    @Operation(summary = "[로그아웃] 로그아웃 API",
        description = """
            ## [로그아웃] 로그아웃 API입니다.
            로그아웃을 진행합니다.
            로그아웃 시, 사용 하던 액세스 토큰과 리프레시 토큰은 더 이상 사용이 불가능합니다.
            다시 서비스를 이용하기 위해서는 로그인을 다시 진행해야 합니다.
            """)
    @PostMapping("/logout")
    public ApiResponse<TokenDTO> logout() {
        return null;
    }

}
