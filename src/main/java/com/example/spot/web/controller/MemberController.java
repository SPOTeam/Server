package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;

import com.example.spot.service.member.MemberService;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberSignInDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberTestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberInfoListDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "Member API")
@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberService memberService;
    @Operation(summary = "!테스트 용! [회원 생성] 테스트 용 회원 생성 API",
        description = """
            ## [테스트 용 회원 생성] 임의의 정보를 가진 회원 객체가 생성 됩니다. 
            다른 API를 테스트 하기 위해 회원이 필요한 경우 사용해주세요.
            회원의 관심 분야 및 지역을 입력 받습니다.   
           생성된 회원의 ID와 Email이 반환 됩니다. """)
    @PostMapping("/member/test")
    public ApiResponse<MemberResponseDTO.MemberTestDTO> testMember(
        @RequestBody @Valid MemberRequestDTO.TestMemberDTO requestDTO){
        MemberTestDTO dto = memberService.testMember(requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }


    @Operation(summary = "!테스트 용! [회원 가입 및 로그인] 카카오 로그인 및 회원가입 ",
        description = """
            ## [회원 가입 및 로그인] 카카오 로그인의 모든 과정이 구현되어 있습니다. 
            가입 테스트를 위해 구현한 테스트 용 카카오 로그인입니다. 
            서버 파트 및 테스트를 원하는 분들은 본 API로 회원 가입 및 로그인을 진행하시면 됩니다. 
            Swagger에서 요청하는 것이 아닌, 브라우저에서 직접 요청해주세요. 
            ## www.teamspot.site/spot/login 
            ## localhost:8080/spot/login  
            
           생성된 회원의 액세스 토큰과 Email이 반환 됩니다. """)
    @GetMapping("/login")
    public void login() throws IOException {
        memberService.redirectURL();
    }

    @Operation(summary = "!서버 용! [회원 가입 및 로그인] 카카오 로그인 및 회원가입 리다이렉트용 API ",
        description = """
            ## [회원 가입 및 로그인] 카카오 로그인의 모든 과정이 구현되어 있습니다. 
            가입 테스트를 위해 구현한 테스트 용 리다이렉트 URL입니다. 
            서버 파트 및 테스트를 원하는 분들은 본 API로 회원 가입 및 로그인을 진행하시면 됩니다. 
            Swagger에서 요청하는 것이 아닌, 브라우저에서 직접 요청해주세요. 
            ## www.teamspot.site/spot/login 
            ## localhost:8080/spot/login  
            
           생성된 회원의 액세스 토큰과 Email이 반환 됩니다. """)
    @GetMapping("/members/sign-in/kakao/redirect")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> redirectURL(@RequestParam String code) throws IOException {
        MemberSignInDTO dto = memberService.signUpByKAKAOForTest(code);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }

    @Operation(summary = "[회원 가입 및 로그인] 카카오 로그인 및 회원가입. ",
        description = """
            ## [회원 가입 및 로그인] 프론트에서 발급 밭은 액세스 토큰을 통해 회원 가입 및 로그인을 진행합니다. 
            연동을 위해 구현된 API입니다. 발급 받은 accessToken을 Param에 첨부하여 API를 호출해주세요.
            생성된 회원의 액세스 토큰과 Email이 반환 됩니다. 
            """)

    @Parameter(name = "accessToken", description = "카카오 액세스 토큰을 입력 해 주세요. ", required = true)
    @GetMapping("/members/sign-in/kakao")
    public ApiResponse<MemberResponseDTO.MemberSignInDTO> signInByKaKao(@RequestParam String accessToken) throws JsonProcessingException {
        MemberSignInDTO dto = memberService.signUpByKAKAO(accessToken);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }

    @PostMapping("/member/{memberId}/theme")
    @Operation(summary = "[회원 정보 업데이트] 관심 분야 입력 및 수정",
        description = """
            ## [회원 정보 업데이트] 해당하는 회원의 관심 분야를 입력 및 수정 합니다.
            테마를 리스트 형식으로 입력 받습니다.
            대상 회원의 식별 아이디와 수정 시각이 반환 됩니다. 
            """,
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "memberId", description = "업데이트할 유저의 ID를 입력 받습니다.", required = true)
    public ApiResponse<MemberUpdateDTO> updateThemes(
        @PathVariable @ExistMember Long memberId,
        @RequestBody @Valid MemberRequestDTO.MemberThemeDTO requestDTO){
        MemberUpdateDTO memberUpdateDTO = memberService.updateTheme(memberId, requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_THEME_UPDATE, memberUpdateDTO);
    }

    @PostMapping("/member/{memberId}/region")
    @Operation(summary = "[회원 정보 업데이트] 관심 지역 입력 및 수정",
        description = """
            ## [회원 정보 업데이트] 해당하는 회원의 관심 지역을 입력 및 수정 합니다.
            지역 코드를 리스트 형식으로 입력 받습니다.
            대상 회원의 식별 아이디와 수정 시각이 반환 됩니다. 
            """,
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "memberId", description = "업데이트할 유저의 ID를 입력 받습니다.", required = true)
    public ApiResponse<MemberUpdateDTO> updateRegions(
        @PathVariable @ExistMember Long memberId,
        @RequestBody @Valid MemberRequestDTO.MemberRegionDTO requestDTO){
        MemberUpdateDTO memberUpdateDTO = memberService.updateRegion(memberId, requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_REGION_UPDATE, memberUpdateDTO);
    }
    @PostMapping("/member/{memberId}/user-info")
    @Operation(summary = "[회원 정보 업데이트] 개인 정보 입력 및 수정",
        description = """
            ## [회원 정보 업데이트] 해당하는 회원의 개인 정보를 입력 및 수정 합니다.
            업데이트 할 회원의 정보를 입력 받습니다.
            대상 회원의 식별 아이디와 수정 시각이 반환 됩니다. 
            """,
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "memberId", description = "업데이트할 유저의 ID를 입력 받습니다.", required = true)
    public ApiResponse<MemberUpdateDTO> updateMemberInfo(
        @PathVariable @ExistMember Long memberId,
        @RequestBody @Valid MemberInfoListDTO requestDTO){
        MemberUpdateDTO memberUpdateDTO = memberService.updateProfile(memberId, requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_INFO_UPDATE, memberUpdateDTO);
    }
}



