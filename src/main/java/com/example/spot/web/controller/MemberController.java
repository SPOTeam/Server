package com.example.spot.web.controller;

import com.example.spot.api.ApiResponse;
import com.example.spot.api.code.status.SuccessStatus;

import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.service.member.MemberService;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberRegionDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberStudyReasonDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberTestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.SocialLoginSignInDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.example.spot.validation.annotation.ExistMember;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.example.spot.web.dto.member.google.GoogleExampleResponse.EXAMPLE_RESPONSE;

@RestController
@RequestMapping("/spot")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberService memberService;
    @Tag(name = "테스트 용 API", description = "테스트 용 API")
    @Operation(summary = "!테스트 용! [회원 생성] 테스트 용 회원 생성 API",
        description = """
            ## [테스트 용 회원 생성] 임의의 정보를 가진 회원 객체가 생성 됩니다. 
            다른 API를 테스트 하기 위해 회원이 필요한 경우 사용해주세요.
            회원의 관심 분야 및 지역을 입력 받습니다.   
           생성된 회원의 ID와 Email이 반환 됩니다. """)
    @PostMapping("/members/test")
    public ApiResponse<MemberResponseDTO.MemberTestDTO> testMember(
        @RequestBody @Valid MemberRequestDTO.MemberInfoListDTO memberInfoListDTO){
        MemberTestDTO dto = memberService.testMember(memberInfoListDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }

    @Tag(name = "테스트 용 API", description = "테스트 용 API")
    @Operation(summary = "!테스트 용! [회원 권한 부여] 관리자 권한 부여 API",
        description = """
            ## [회원 권한 부여] 해당하는 회원에게 관리자 권한을 부여합니다.
            테스트를 위해 구현한 테스트 용 API입니다.
            회원의 ID를 입력 받아 관리자 권한을 부여합니다.
            성공 여부와 회원 ID가 반환 됩니다. 
             """)
    @PostMapping("/members/test/admin")
    public ApiResponse<MemberResponseDTO.MemberUpdateDTO> toAdmin(){
        MemberUpdateDTO dto = memberService.toAdmin(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }


    @Tag(name = "테스트 용 API", description = "테스트 용 API")
    @Operation(summary = "!테스트 용! [회원 가입 및 로그인] 카카오 로그인 및 회원가입 ",
        description = """
            ## [회원 가입 및 로그인] 카카오 로그인의 모든 과정이 구현되어 있습니다. 
            가입 테스트를 위해 구현한 테스트 용 카카오 로그인입니다. 
            서버 파트 및 테스트를 원하는 분들은 본 API로 회원 가입 및 로그인을 진행하시면 됩니다. 
            Swagger에서 요청하는 것이 아닌, 브라우저에서 직접 요청해주세요. 
            ## www.teamspot.site/spot/login/kakao 
            ## localhost:8080/spot/login/kakao  
            
           생성된 회원의 액세스 토큰과 Email이 반환 됩니다. """)
    @GetMapping("/login/kakao")
    public void login() throws IOException {
        memberService.redirectURL();
    }

    @Tag(name = "테스트 용 API", description = "테스트 용 API")
    @Operation(summary = "!서버 용! [회원 가입 및 로그인] 카카오 로그인 및 회원가입 리다이렉트용 API ",
        description = """
            ## [회원 가입 및 로그인] 카카오 로그인의 모든 과정이 구현되어 있습니다. 
            가입 테스트를 위해 구현한 테스트 용 리다이렉트 URL입니다. 
            서버 파트 및 테스트를 원하는 분들은 본 API로 회원 가입 및 로그인을 진행하시면 됩니다. 
            Swagger에서 요청하는 것이 아닌, 브라우저에서 직접 요청해주세요. 
            ## www.teamspot.site/spot/login/kakao
            ## localhost:8080/spot/login/kakao  
            
           생성된 회원의 액세스 토큰과 Email이 반환 됩니다. """)
    @GetMapping("/members/sign-in/kakao/redirect")
    public ApiResponse<MemberResponseDTO.SocialLoginSignInDTO> redirectURL(@RequestParam String code) throws IOException {
        SocialLoginSignInDTO dto = memberService.signUpByKAKAOForTest(code);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }

    @Tag(name = "회원 관리 API", description = "회원 관리 API")
    @Operation(summary = "[회원 가입 및 로그인] 카카오 로그인 및 회원가입. ",
        description = """
            ## [회원 가입 및 로그인] 프론트에서 발급 밭은 액세스 토큰을 통해 회원 가입 및 로그인을 진행합니다. 
            연동을 위해 구현된 API입니다. 발급 받은 accessToken을 Param에 첨부하여 API를 호출해주세요.
            생성된 회원의 액세스 토큰과 Email이 반환 됩니다. 
            """)

    @Parameter(name = "accessToken", description = "카카오 액세스 토큰을 입력 해 주세요. ", required = true)
    @GetMapping("/members/sign-in/kakao")
    public ApiResponse<MemberResponseDTO.SocialLoginSignInDTO> signInByKaKao(@RequestParam String accessToken) throws JsonProcessingException {
        SocialLoginSignInDTO dto = memberService.signUpByKAKAO(accessToken);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_CREATED, dto);
    }

    @Tag(name = "회원 관리 API", description = "회원 관리 API")
    @PostMapping("/members/theme")
    @Operation(summary = "[회원 정보 업데이트] 관심 분야 입력 및 수정",
        description = """
            ## [회원 정보 업데이트] 해당하는 회원의 관심 분야를 입력 및 수정 합니다.
            테마를 리스트 형식으로 입력 받습니다.
            대상 회원의 식별 아이디와 수정 시각이 반환 됩니다. 
            """,
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "memberId", description = "업데이트할 유저의 ID를 입력 받습니다.", required = true)
    public ApiResponse<MemberUpdateDTO> updateThemes(
        @RequestBody @Valid MemberRequestDTO.MemberThemeDTO requestDTO){
        MemberUpdateDTO memberUpdateDTO = memberService.updateTheme(SecurityUtils.getCurrentUserId(), requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_THEME_UPDATE, memberUpdateDTO);
    }

    @Tag(name = "회원 관리 API", description = "회원 관리 API")
    @PostMapping("/members/region")
    @Operation(summary = "[회원 정보 업데이트] 관심 지역 입력 및 수정",
        description = """
            ## [회원 정보 업데이트] 해당하는 회원의 관심 지역을 입력 및 수정 합니다.
            지역 코드를 리스트 형식으로 입력 받습니다.
            대상 회원의 식별 아이디와 수정 시각이 반환 됩니다. 
            """,
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "memberId", description = "업데이트할 유저의 ID를 입력 받습니다.", required = true)
    public ApiResponse<MemberUpdateDTO> updateRegions(
        @RequestBody @Valid MemberRequestDTO.MemberRegionDTO requestDTO){
        MemberUpdateDTO memberUpdateDTO = memberService.updateRegion(SecurityUtils.getCurrentUserId(), requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_REGION_UPDATE, memberUpdateDTO);
    }
    @Tag(name = "회원 관리 API", description = "회원 관리 API")
    @PostMapping("/members/user-info")
    @Operation(summary = "[회원 정보 업데이트] 개인 정보 입력 및 수정",
        description = """
            ## [회원 정보 업데이트] 해당하는 회원의 개인 정보를 입력 및 수정 합니다.
            업데이트 할 회원의 정보를 입력 받습니다.
            대상 회원의 식별 아이디와 수정 시각이 반환 됩니다. 
            """,
        security = @SecurityRequirement(name = "accessToken"))
    @Parameter(name = "memberId", description = "업데이트할 유저의 ID를 입력 받습니다.", required = true)
    public ApiResponse<MemberUpdateDTO> updateMemberInfo(
        @RequestBody @Valid MemberRequestDTO.MemberUpdateDTO requestDTO){
        MemberUpdateDTO memberUpdateDTO = memberService.updateProfile(SecurityUtils.getCurrentUserId(), requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_INFO_UPDATE, memberUpdateDTO);
    }

    @Tag(name = "회원 관리 API", description = "회원 관리 API")
    @PostMapping("/members/study-reasons")
    @Operation(summary = "[회원 정보 업데이트] 스터디 이유 입력 및 수정",
        description = """
            ## [회원 정보 업데이트] 해당하는 회원의 스터디 이유를 입력 및 수정 합니다.
            업데이트 할 회원의 정보를 입력 받습니다.
            
            1. 동기부여를_받고_싶어요
            2. 스터디원이_필요해요
            3. 혼자서_하기가_의지가_부족해요
            4. 한_목표를_가진_사람들과_친해지고_싶어요
            5. 다양한_정보를_공유하고_받고_싶어요
            
            이유에 해당하는 숫자를 리스트 형식으로 입력 받습니다.
            
            대상 회원의 식별 아이디와 수정 시각이 반환 됩니다. 
            """,
        security = @SecurityRequirement(name = "accessToken"))
    public ApiResponse<MemberUpdateDTO> updateMemberStudyReason(
        @RequestBody @Valid MemberRequestDTO.MemberReasonDTO requestDTO){
        MemberUpdateDTO memberUpdateDTO = memberService.updateStudyReason(SecurityUtils.getCurrentUserId(), requestDTO);
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_INFO_UPDATE, memberUpdateDTO);
    }

    @Tag(name = "회원 조회 API", description = "회원 조회 API")
    @GetMapping("/members/theme")
    @Operation(summary = "[회원 정보 조회] 관심 분야 조회",
        description = """
            ## [회원 정보 조회] 해당하는 회원의 관심 분야를 조회 합니다.
            
            관심 분야를 리스트 형식으로 응답합니다.
            """,
        security = @SecurityRequirement(name = "accessToken"))
    public ApiResponse<MemberResponseDTO.MemberThemeDTO> getThemes(){
        MemberResponseDTO.MemberThemeDTO memberThemeDTO = memberService.getThemes(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_THEME_UPDATE, memberThemeDTO);
    }

    @Tag(name = "회원 조회 API", description = "회원 조회 API")
    @GetMapping("/members/region")
    @Operation(summary = "[회원 정보 조회] 관심 지역 조회",
        description = """
            ## [회원 정보 조회] 해당하는 회원의 관심 지역을 조회 합니다.
            
            관심 지역을 리스트 형식으로 응답합니다.
            """,
        security = @SecurityRequirement(name = "accessToken"))
    public ApiResponse<MemberResponseDTO.MemberRegionDTO> getRegions(){
        MemberRegionDTO memberRegionDTO = memberService.getRegions(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_REGION_UPDATE, memberRegionDTO);
    }

    @Tag(name = "회원 조회 API", description = "회원 조회 API")
    @GetMapping("/members/study-reasons")
    @Operation(summary = "[회원 정보 조회] 스터디 이유 조회",
        description = """
            ## [회원 정보 조회] 해당하는 회원의 스터디 이유를 조회 합니다.
            
            스터디 이유를 리스트 형식으로 응답합니다.
            """,
        security = @SecurityRequirement(name = "accessToken"))
    public ApiResponse<MemberResponseDTO.MemberStudyReasonDTO> getStudyReasons(){
        MemberStudyReasonDTO memberStudyReasonDTO = memberService.getStudyReasons(SecurityUtils.getCurrentUserId());
        return ApiResponse.onSuccess(SuccessStatus._MEMBER_REGION_UPDATE, memberStudyReasonDTO);
    }


    @Tag(name = "구글 로그인 API", description = "구글 OAuth2 로그인 API")
    @Operation(summary = "[구글 로그인] 구글 로그인/회원가입 API",
            description = """
               구글 로그인 인증 페이지로 이동합니다.
               사용자가 로그인 후, 설정된 리디렉션 URL로 돌아옵니다.
               브라우저에서 직접 요청해 주세요.
               ## http://localhost:8080/oauth2/authorization/google
               ## www.teamspot.site/oauth2/authorization/google
               """)
    @GetMapping("/oauth/authorize")
    public void redirectToGoogleLogin(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", "/oauth/authorize");
    }

    @Tag(name = "구글 로그인 API", description = "구글 OAuth2 로그인 API")
    @Operation(summary = "[구글 로그인] 구글 로그인/회원가입 리다이렉트용 API",
            description = """
               구글 로그인 인증 완료 후 호출되는 콜백 URL입니다.
               클라이언트가 직접 호출하지 않습니다.
               로그인 성공 시 회원의 이메일과 토큰 정보를 반환합니다.
               """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "google OAuth 로그인에 성공하면 SPOT 서버에 접근할 수 있는 SPOT JWT Token을 반환합니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponses.class),
                            examples = @ExampleObject(
                                    value = EXAMPLE_RESPONSE
                            )))})
    @GetMapping("/members/sign-in/google/redirect")
    public void handleGoogleCallback() {
    }

}
