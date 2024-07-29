package com.example.spot.service.member;

import com.example.spot.repository.MemberRepository;
import com.example.spot.service.member.oauth.KaKaoOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class MemberCommandServiceImpl implements MemberCommandService {

    private final KaKaoOAuthService kaKaoOAuthService;
    private final MemberRepository memberRepository;

    @Override
    public void signUpByKAKAO(String code) {

    }
}
