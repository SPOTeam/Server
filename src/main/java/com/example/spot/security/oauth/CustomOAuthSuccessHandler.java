package com.example.spot.security.oauth;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.SocialLoginSignInDTO;
import com.example.spot.security.oauth.adpter.CustomOAuth2User;
import com.example.spot.security.oauth.adpter.google.GoogleUserInfo;
import com.example.spot.web.dto.token.TokenResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * Spring Security가 CustomOAuth2UserService.loadUser()에서 성공한 사용자 정보를 자동으로 SecurityContext Principle에 등록합니다.
 * 따라서, OAuth로 로그인 성공 후 브라우저 재 진입시 Principle이 살아있는한 다시 로그인할 필요가 없습니다. (JwtToken과는 별개)
 */

@Component
@RequiredArgsConstructor
public class CustomOAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // CustomOAuth2UserService.loadUser()에서 저장한 Security Context에서 Principle 추출
        // 추후에 Spring Security에서 제공하는 OAuth 방식을 사용할까봐, OAuth2UserInfo로 추상화했습니다.
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        GoogleUserInfo googleUserInfo = new GoogleUserInfo(customOAuth2User.getAttributes());

        String email = googleUserInfo.getEmail();
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        Member member = memberOptional.orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Long memberId = member.getId();

        // JWT Token 발급
        TokenResponseDTO.TokenDTO token = jwtTokenProvider.createToken(memberId);

        MemberResponseDTO.MemberSignInDTO memberSignInDTO = MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(token)
                .memberId(member.getId())
                .loginType(member.getLoginType())
                .email(member.getEmail())
                .build();

        // OAuth 로그인 성공한 후 응답 방식으로 포멧팅
        ApiResponse<MemberResponseDTO.SocialLoginSignInDTO> apiResponse = ApiResponse.onSuccess(
                SuccessStatus._OK, SocialLoginSignInDTO.toDTO(
                        customOAuth2User.getIsSpotMember(),memberSignInDTO));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 객체 직렬화하여 작성
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
