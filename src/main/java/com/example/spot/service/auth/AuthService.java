package com.example.spot.service.auth;

import com.example.spot.web.dto.member.MemberRequestDTO.SignUpDetailDTO;
import com.example.spot.web.dto.rsa.Rsa;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.SocialLoginSignInDTO;
import com.example.spot.web.dto.member.naver.NaverCallback;
import com.example.spot.web.dto.member.naver.NaverOAuthToken;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
    TokenDTO reissueToken(String refreshToken);

    MemberResponseDTO.MemberInfoCreationDTO signUpAndPartialUpdate(SignUpDetailDTO sign);

    MemberResponseDTO.InactiveMemberDTO withdraw();

    void authorizeWithNaver(HttpServletRequest request, HttpServletResponse response);

    SocialLoginSignInDTO signInWithNaver(HttpServletRequest request, HttpServletResponse response, NaverCallback naverCallback) throws Exception;

    SocialLoginSignInDTO signInWithNaver(HttpServletRequest request, HttpServletResponse response, NaverOAuthToken.NaverTokenIssuanceDTO naverTokenDTO) throws Exception;

    MemberResponseDTO.MemberSignInDTO signIn(Long httpSession, MemberRequestDTO.SignInDTO signInDTO) throws Exception;

    Rsa.RSAPublicKey getRSAPublicKey() throws Exception;

    void sendVerificationCode(HttpServletRequest request, HttpServletResponse response, String email);

    TokenResponseDTO.TempTokenDTO verifyEmail(String verificationCode, String email);

    MemberResponseDTO.MemberSignInDTO signUp(Long rsaId, MemberRequestDTO.SignUpDTO signUpDTO) throws Exception;

    MemberResponseDTO.FindIdDTO findId();

    MemberResponseDTO.FindPwDTO findPw(String loginId);

    MemberResponseDTO.AvailabilityDTO checkLoginIdAvailability(String loginId);

    MemberResponseDTO.AvailabilityDTO checkEmailAvailability(String email);

    MemberResponseDTO.CheckMemberDTO checkIsSpotMember(Long loginId);

    MemberResponseDTO.NicknameDuplicateDTO checkNicknameAvailability(String nickname);
}
