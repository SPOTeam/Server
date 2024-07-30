package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;
import com.example.spot.service.member.MemberService;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberSignInDTO;
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

    private final MemberService memberService;

    @GetMapping("/login")
    public void login() throws IOException {
        memberService.redirectURL();
    }

    @GetMapping("/members/sign-in/kakao")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> signInByKaKao(@RequestParam String code) throws JsonProcessingException {
        MemberSignInDTO dto = memberService.signUpByKAKAO(code);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }
}



