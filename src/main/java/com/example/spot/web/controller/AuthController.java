package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.auth.AuthService;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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

    @Tag(name = "회원 관리 API - 개발 중", description = "회원 관리 API")
    @Operation(summary = "[회원 가입] 일반 회원 가입 API",
        description = """
            ## [회원 가입] 일반 회원 가입 API입니다.
            회원 가입 시, 아이디(이메일)과 비밀번호를 입력하여 회원 가입을 진행합니다.
            회원 가입에 성공하면, 액세스 토큰과 리프레시 토큰이 발급됩니다.
            액세스 토큰은 사용자의 정보를 인증하는데 사용되며, 리프레시 토큰은 액세스 토큰이 만료된 경우, 액세스 토큰을 재발급 하는데 사용됩니다.
            액세스 토큰이 만료된 경우, 유효한 상태의 리프레시 토큰을 통해 액세스 토큰을 재발급 받을 수 있습니다.
            """)
    @PostMapping("/sign-up")
    public ApiResponse<TokenDTO> signUp() {
        return null;
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
    public ApiResponse<TokenDTO> login() {
        return null;
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
