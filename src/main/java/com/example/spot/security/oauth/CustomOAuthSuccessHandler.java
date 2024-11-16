package com.example.spot.security.oauth;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.google.CustomOAuth2User;
import com.example.spot.web.dto.member.google.GoogleUserInfo;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.net.URLEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("flag1 CustomOAuthSuccessHandler.onAuthenticationSuccess");
        System.out.println("로그인 성공");

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(customOAuth2User.getAttributes());
        String email = googleUserInfo.getEmail();

        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        Member member = memberOptional.orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        Long memberId = member.getId();

        // 인증이 성공했을 때, 어세스 토큰과 리프레시 토큰 발급
        TokenResponseDTO.TokenDTO token = jwtTokenProvider.createToken(memberId);

        MemberResponseDTO.MemberSignInDTO memberSignInDTO = MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(token)
                .memberId(member.getId())
                .email(member.getEmail())
                .build();

        ApiResponse<MemberResponseDTO.MemberSignInDTO> apiResponse = ApiResponse.onSuccess(SuccessStatus._OK, memberSignInDTO);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 객체를 JSON으로 변환하여 응답에 작성
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
