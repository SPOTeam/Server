package com.example.spot.web.service.kakao;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.jwt.JwtTokenProvider;
import com.example.spot.jwt.MemberAuthentication;
import com.example.spot.repository.MemberRepository;
import com.example.spot.web.dto.user.KakaoApiClient;
import com.example.spot.web.dto.user.KakaoUserResponse;
import com.example.spot.web.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoUserService {

    private final MemberRepository memberRepository;
    private final KakaoApiClient kakaoApiClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    @Transactional
    //로그인 로직 구현
    public ApiResponse<String> login(final String accessToken){

        KakaoUserResponse response = kakaoApiClient.getUserInfo("Bearer" + accessToken);

        Long memberId = memberService.createUser(response);
        MemberAuthentication memberAuthentication = new MemberAuthentication(memberId.toString(), null, null);
        return ApiResponse.onSuccess(SuccessStatus._OK, jwtTokenProvider.generateToken(memberAuthentication));
    }
    


}
