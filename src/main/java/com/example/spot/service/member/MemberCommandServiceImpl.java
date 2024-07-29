package com.example.spot.service.member;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.repository.MemberRepository;
import com.example.spot.service.member.oauth.KaKaoOAuthService;
import com.example.spot.web.dto.member.kakao.KaKaoOAuthToken.KaKaoOAuthTokenDTO;
import com.example.spot.web.dto.member.kakao.KaKaoUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class MemberCommandServiceImpl implements MemberCommandService {

    private final KaKaoOAuthService kaKaoOAuthService;
    private final HttpServletResponse response;
    private final MemberRepository memberRepository;

    @Override
    public void signUpByKAKAO(String code) throws JsonProcessingException {
        ResponseEntity<String> accessTokenResponse = kaKaoOAuthService.requestAccessToken(code);
        KaKaoOAuthTokenDTO oAuthToken = kaKaoOAuthService.getAccessToken(accessTokenResponse);
        ResponseEntity<String> userInfoResponse = kaKaoOAuthService.requestUserInfo(oAuthToken);
        KaKaoUser kaKaoUser = kaKaoOAuthService.getUserInfo(userInfoResponse);

        if (memberRepository.existsByEmail(kaKaoUser.toMember().getEmail()))
            throw new GeneralException(ErrorStatus._MEMBER_EMAIL_ALREADY_EXISTS);
        memberRepository.save(kaKaoUser.toMember());
    }

    @Override
    public void redirectURL() throws IOException {
        response.sendRedirect(kaKaoOAuthService.getOauthRedirectURL());
    }
}
