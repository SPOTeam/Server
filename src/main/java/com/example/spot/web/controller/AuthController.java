package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.auth.AuthService;
import com.example.spot.validation.annotation.TextLength;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            * 이메일 인증 API로부터 발급 받은 임시 토큰이 Authorization 헤더에 포함되어야 합니다.
            * 회원 가입에 성공하면, 액세스 토큰과 리프레시 토큰이 발급됩니다.
            * 액세스 토큰은 사용자의 정보를 인증하는데 사용되며, 리프레시 토큰은 액세스 토큰이 만료된 경우, 액세스 토큰을 재발급 하는데 사용됩니다.
            * 액세스 토큰이 만료된 경우, 유효한 상태의 리프레시 토큰을 통해 액세스 토큰을 재발급 받을 수 있습니다.
            """)
    @PostMapping("/sign-up")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> signUp(
            @RequestBody @Valid MemberRequestDTO.SignUpDTO signUpDTO) {
        MemberResponseDTO.MemberSignInDTO memberSignUpDTO = authService.signUp(signUpDTO);
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
    @Operation(summary = "[로그인] 일반 로그인 API",
        description = """
            ## [로그인] 아이디(이메일)과 비밀번호를 통해 로그인 하는 API입니다.
            로그인에 성공하면, 액세스 토큰과 리프레시 토큰이 발급됩니다.
            액세스 토큰은 사용자의 정보를 인증하는데 사용되며, 리프레시 토큰은 액세스 토큰이 만료된 경우, 액세스 토큰을 재발급 하는데 사용됩니다.
            액세스 토큰이 만료된 경우, 유효한 상태의 리프레시 토큰을 통해 액세스 토큰을 재발급 받을 수 있습니다.
            """)
    @PostMapping("/login")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> login(
            @RequestBody @Valid MemberRequestDTO.SignInDTO signInDTO) {
        MemberResponseDTO.MemberSignInDTO memberSignInDTO = authService.signIn(signInDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_SIGNED_IN, memberSignInDTO);
    }

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
