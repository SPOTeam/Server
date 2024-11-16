package com.example.spot.security.oauth;

import com.example.spot.domain.Member;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.LoginType;
import com.example.spot.repository.MemberRepository;
import com.example.spot.security.utils.MemberUtils;
import com.example.spot.service.member.MemberService;
import com.example.spot.web.dto.member.google.CustomOAuth2User;
import com.example.spot.web.dto.member.google.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuthUserInfo = OAuthUserInfoFactory.getOAuthUserInfo(provider, attributes);

        String oauthEmail = oAuthUserInfo.getEmail();
        Optional<Member> optionalMember = memberRepository.findByEmail(oauthEmail);
        if (optionalMember.isEmpty()) {
            if (provider.equals("google")) {
                Member newMember = Member.builder()
                        .name(attributes.get("name").toString())
                        .nickname(attributes.get("name").toString())
                        .email(oauthEmail)
                        .profileImage(attributes.get("picture").toString())
                        .carrier(Carrier.NONE)
                        .password("default")
                        .phone(MemberUtils.generatePhoneNumber())
                        .birth(LocalDate.now())
                        .personalInfo(false)
                        .idInfo(false)
                        .isAdmin(false)
                        .loginType(LoginType.GOOGLE)
                        .build();
                memberService.save(newMember);
                return new CustomOAuth2User(newMember, attributes);
            }
            throw new RuntimeException("제공하지 않는 로그인타입 입니다.");
        }

        return new CustomOAuth2User(optionalMember.get(), attributes);
    }
}
