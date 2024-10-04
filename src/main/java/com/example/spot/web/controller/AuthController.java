package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.auth.AuthService;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    @Operation(summary = "[회원 가입] 일반 회원 가입 인증번호 전송 API",
            description = """
            ## [회원 가입] 일반 회원 가입 인증번호 전송 API입니다.
            입력받은 전화번호로 인증번호가 전송됩니다.
            """)
    @PostMapping("/sign-up/send-verification-code")
    public ApiResponse<Void> sendVerificationCode(
            @RequestBody @Valid MemberRequestDTO.PhoneDTO phoneDTO) {
        authService.sendVerificationCode(phoneDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_PHONE_VERIFIED);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[회원 가입] 일반 회원 가입 전화번호 인증 API",
            description = """
            ## [회원 가입] 일반 회원 가입 전화번호 인증 API입니다.
            사용자로부터 인증코드와 전화번호를 받아 검증 작업을 수행한 후, 임시 토큰을 반환합니다.
            임시 토큰은 최대 3분간 유효합니다. 임시 토큰이 만료된 경우 전화번호 재인증이 필요합니다.
            """)
    @PostMapping("/sign-up/verify")
    public ApiResponse<TokenResponseDTO.TempTokenDTO> verifyPhone(
            @RequestParam String verificationCode,
            @RequestBody @Valid MemberRequestDTO.PhoneDTO phoneDTO) {
        TokenResponseDTO.TempTokenDTO tempTokenDTO = authService.verifyPhone(verificationCode, phoneDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_PHONE_VERIFIED, tempTokenDTO);
    }

    @Tag(name = "회원 관리 API - 개발 완료", description = "회원 관리 API")
    @Operation(summary = "[회원 가입] 일반 회원 가입 API",
        description = """
            ## [회원 가입] 일반 회원 가입 API입니다.
            아이디(이메일)과 비밀번호를 입력하여 회원 가입을 진행합니다.
            전화번호 인증 API로부터 발급 받은 임시 토큰이 Authorization 헤더에 포함되어야 합니다.
            회원 가입에 성공하면, 액세스 토큰과 리프레시 토큰이 발급됩니다.
            액세스 토큰은 사용자의 정보를 인증하는데 사용되며, 리프레시 토큰은 액세스 토큰이 만료된 경우, 액세스 토큰을 재발급 하는데 사용됩니다.
            액세스 토큰이 만료된 경우, 유효한 상태의 리프레시 토큰을 통해 액세스 토큰을 재발급 받을 수 있습니다.
            """)
    @PostMapping("/sign-up")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> signUp(
            @RequestBody @Valid MemberRequestDTO.SignUpDTO signUpDTO) {
        MemberResponseDTO.MemberSignInDTO memberSignUpDTO = authService.signUp(signUpDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, memberSignUpDTO);
    }

    @Tag(name = "회원 관리 API - 개발 중", description = "회원 관리 API")
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
