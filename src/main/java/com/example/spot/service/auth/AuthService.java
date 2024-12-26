package com.example.spot.service.auth;

import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.SocialLoginSignInDTO;
import com.example.spot.web.dto.member.naver.NaverCallback;
import com.example.spot.web.dto.member.naver.NaverOAuthToken;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
    TokenDTO reissueToken(String refreshToken);

    MemberResponseDTO.MemberUpdateDTO signUpAndPartialUpdate(String nickname, Boolean personalInfo, Boolean idInfo);

    void authorizeWithNaver(HttpServletRequest request, HttpServletResponse response);

    SocialLoginSignInDTO signInWithNaver(HttpServletRequest request, HttpServletResponse response, NaverCallback naverCallback) throws JsonProcessingException;

    SocialLoginSignInDTO signInWithNaver(HttpServletRequest request, HttpServletResponse response, NaverOAuthToken.NaverTokenIssuanceDTO naverTokenDTO) throws JsonProcessingException;

    MemberResponseDTO.MemberSignInDTO signIn(MemberRequestDTO.SignInDTO signInDTO);

    void sendVerificationCode(HttpServletRequest request, HttpServletResponse response, String email);

    TokenResponseDTO.TempTokenDTO verifyEmail(String verificationCode, String email);

    MemberResponseDTO.MemberSignInDTO signUp(MemberRequestDTO.SignUpDTO signUpDTO);

    MemberResponseDTO.FindIdDTO findId();

    MemberResponseDTO.FindPwDTO findPw(String loginId);

    MemberResponseDTO.AvailabilityDTO checkLoginIdAvailability(String loginId);

    MemberResponseDTO.AvailabilityDTO checkEmailAvailability(String email);

}
