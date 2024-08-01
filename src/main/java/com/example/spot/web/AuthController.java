package com.example.spot.web;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Auth API")
@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/reissue")
    public ApiResponse<TokenDTO> reissueToken(@RequestHeader String refreshToken) {
        return ApiResponse.onSuccess(SuccessStatus._CREATED, jwtTokenProvider.reissueToken(refreshToken));
    }


}
