package com.example.spot.security.oauth;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.LoginType;
import com.example.spot.repository.MemberRepository;
import com.example.spot.security.utils.MemberUtils;
import com.example.spot.service.member.MemberService;
import com.example.spot.security.oauth.adpter.CustomOAuth2User;
import com.example.spot.security.oauth.adpter.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;


/**
 * CustomOAuth2UserService는 OAuth2UserService를 확장하여, 액세스 토큰을 사용하여 사용자 정보를 가져오는 역할을 합니다.
 *
 * loadUser() 메서드: 이 메서드는 액세스 토큰을 사용하여 구글 API에서 사용자 정보를 가져옵니다.
 * 사용자 정보 처리: 가져온 사용자 정보를 사용하여 새로운 사용자를 생성하거나 기존 사용자와 연동합니다.
 *
 * 정리하면, accessToken으로 Oauth에게 받아온 사용자 정보를 가져오고 처리하는 역할을합니다.
 */


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // Google이 발급한 accessToken으로 요청한 후 받은 사용자에 대한 Google 정보 추출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 로그인한 경로, 이메일 등 필요한 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuthUserInfo = OAuthUserInfoFactory.getOAuthUserInfo(provider, attributes);

        String oauthEmail = oAuthUserInfo.getEmail();
        if (memberRepository.existsByEmailAndLoginTypeNot(oauthEmail, LoginType.GOOGLE))
            throw new GeneralException(ErrorStatus._MEMBER_EMAIL_EXIST);

        //
        Optional<Member> optionalMember = memberRepository.findByEmail(oauthEmail);
        if (optionalMember.isEmpty()) {
            if (provider.equals("google")) {
                Member newMember = generateMember(attributes, oauthEmail);
                memberService.save(newMember);

                // SuccessHandler에서 principle로 추출시에 우리 회원 정보를 추출하기 위해 CustomOAuth2User로 반환
                return new CustomOAuth2User(newMember, attributes, false);
            }
            throw new MemberHandler(ErrorStatus._MEMBER_UNSUPPORTED_LOGIN_TYPE);
        }

        // SuccessHandler에서 principle로 추출시에 우리 회원 정보를 추출하기 위해 CustomOAuth2User로 반환
        return new CustomOAuth2User(optionalMember.get(), attributes, true);
    }

    private Member generateMember(Map<String, Object> attributes, String oauthEmail) {
        return Member.builder()
                .name(attributes.get("name").toString().substring(0, 10))
                .nickname(attributes.get("name").toString().substring(0, 10))
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
    }
}
