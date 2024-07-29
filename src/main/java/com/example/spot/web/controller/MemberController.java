package com.example.spot.web.controller;

import com.example.spot.service.member.MemberCommandService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "Member API")
@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
public class MemberController {

    private final MemberCommandService memberCommandService;

    @GetMapping("/login")
    public void login() throws IOException {
        memberCommandService.redirectURL();
    }
    @GetMapping("/members/sign-in/kakao")
    public String signInByKaKao(@RequestParam String code) throws JsonProcessingException {
        memberCommandService.signUpByKAKAO(code);
        return null;
    }
}



