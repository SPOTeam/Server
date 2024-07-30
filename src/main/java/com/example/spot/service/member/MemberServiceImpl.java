package com.example.spot.service.member;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.config.jwt.JwtTokenProvider;
import com.example.spot.domain.Member;
import com.example.spot.domain.enums.Status;
import com.example.spot.repository.MemberRepository;
import com.example.spot.service.member.oauth.KaKaoOAuthService;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.kakao.KaKaoOAuthToken.KaKaoOAuthTokenDTO;
import com.example.spot.web.dto.member.kakao.KaKaoUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final KaKaoOAuthService kaKaoOAuthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final HttpServletResponse response;
    private final MemberRepository memberRepository;

    @Override
    public MemberResponseDTO.MemberSignInDTO signUpByKAKAO(String code) throws JsonProcessingException {
        // 카카오 OAuth 서비스에서 액세스 토큰 요청
        ResponseEntity<String> accessTokenResponse = kaKaoOAuthService.requestAccessToken(code);

        // 응답에서 액세스 토큰을 파싱
        KaKaoOAuthTokenDTO oAuthToken = kaKaoOAuthService.getAccessToken(accessTokenResponse);
        System.out.println(oAuthToken.getAccess_token());

        // 액세스 토큰을 사용하여 사용자 정보 요청
        ResponseEntity<String> userInfoResponse = kaKaoOAuthService.requestUserInfo(oAuthToken);

        // 응답에서 사용자 정보를 파싱
        KaKaoUser kaKaoUser = kaKaoOAuthService.getUserInfo(userInfoResponse);

        // 사용자가 이미 존재하는지 확인
        if (memberRepository.existsByEmail(kaKaoUser.toMember().getEmail())) {
            // 존재하는 경우, 사용자 정보를 가져옴
            Member member = memberRepository.findByEmail(kaKaoUser.toMember().getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

            // JWT 토큰 생성
            String token = jwtTokenProvider.createToken(member.getEmail());

            // 로그인 DTO 반환
            return MemberResponseDTO.MemberSignInDTO.builder()
                .accessToken(token)
                .email(member.getEmail())
                .build();
        }

        // 존재하지 않는 경우, 새로운 회원 정보 저장
        Member member = memberRepository.save(kaKaoUser.toMember());

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(member.getEmail());

        // 회원 가입 DTO 반환
        return MemberResponseDTO.MemberSignInDTO.builder()
            .accessToken(token)
            .email(member.getEmail())
            .build();
    }

    @Override
    public void redirectURL() throws IOException {
        response.sendRedirect(kaKaoOAuthService.getOauthRedirectURL());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + (member.getIsAdmin() ? "ADMIN" : "USER"))
        );

        return new User(member.getEmail(), member.getPassword(),
            true, true,
            true, true, authorities);
    }

    @Override
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));
    }

    @Override
    public boolean isMemberExists(String email) {
        return memberRepository.existsByEmail(email);
    }
    public Authentication authenticate(String token) {
        String userEmail = jwtTokenProvider.getUserPk(token);
        UserDetails userDetails = loadUserByUsername(userEmail);
        return jwtTokenProvider.getAuthentication(token, userDetails);
    }
}
