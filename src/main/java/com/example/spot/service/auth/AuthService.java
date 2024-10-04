package com.example.spot.service.auth;

import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;

public interface AuthService {

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
    TokenDTO reissueToken(String refreshToken);

    MemberResponseDTO.MemberSignInDTO signIn(MemberRequestDTO.SignInDTO signInDTO);

    void sendVerificationCode(MemberRequestDTO.PhoneDTO phoneDTO);

    TokenResponseDTO.TempTokenDTO verifyPhone(String verificationCode, MemberRequestDTO.PhoneDTO phoneDTO);

    MemberResponseDTO.MemberSignInDTO signUp(MemberRequestDTO.SignUpDTO signUpDTO);

}
