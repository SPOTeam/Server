package com.example.spot.service.member;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.domain.StudyReason;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.Reason;
import com.example.spot.domain.enums.Status;
import com.example.spot.repository.StudyReasonRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberReasonDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.TestMemberDTO;
import com.example.spot.domain.auth.CustomUserDetails;
import com.example.spot.domain.auth.RefreshToken;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.service.auth.KaKaoOAuthService;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberSignInDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberTestDTO;
import com.example.spot.web.dto.member.kakao.KaKaoOAuthToken.KaKaoOAuthTokenDTO;
import com.example.spot.web.dto.member.kakao.KaKaoUser;
import com.example.spot.web.dto.token.TokenResponseDTO.TokenDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.mapping.MemberTheme;
import com.example.spot.domain.mapping.PreferredRegion;
import com.example.spot.repository.MemberThemeRepository;
import com.example.spot.repository.PreferredRegionRepository;
import com.example.spot.repository.RegionRepository;
import com.example.spot.repository.ThemeRepository;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberInfoListDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberRegionDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberThemeDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberUpdateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    // OAuth
    private final KaKaoOAuthService kaKaoOAuthService;

    // JWT
    private final JwtTokenProvider jwtTokenProvider;

    // Response
    private final HttpServletResponse response;

    // Repository
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final RegionRepository regionRepository;
    private final MemberThemeRepository memberThemeRepository;
    private final PreferredRegionRepository preferredRegionRepository;
    private final StudyReasonRepository studyReasonRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public MemberResponseDTO.MemberSignInDTO signUpByKAKAO(String accessToken) throws JsonProcessingException {
        // 액세스 토큰을 사용하여 사용자 정보 요청
        ResponseEntity<String> userInfoResponse = kaKaoOAuthService.requestUserInfo(accessToken);

        // 응답에서 사용자 정보를 파싱
        KaKaoUser kaKaoUser = kaKaoOAuthService.getUserInfo(userInfoResponse);

        // 사용자가 이미 존재하는지 확인
        if (memberRepository.existsByEmail(kaKaoUser.toMember().getEmail())) {
            // 존재하는 경우, 사용자 정보를 가져옴
            Member member = memberRepository.findByEmail(kaKaoUser.toMember().getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

            // JWT 토큰 생성
            TokenDTO token = jwtTokenProvider.createToken(member.getId());

            saveRefreshToken(member, token);

            // 로그인 DTO 반환
            return MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(token)
                .memberId(member.getId())
                .email(member.getEmail())
                .build();
        }


        // 존재하지 않는 경우, 새로운 회원 정보 저장
        Member member = memberRepository.save(kaKaoUser.toMember());

        // JWT 토큰 생성
        TokenDTO token = jwtTokenProvider.createToken(member.getId());

        saveRefreshToken(member, token);

        // 회원 가입 DTO 반환
        return MemberResponseDTO.MemberSignInDTO.builder()
            .tokens(token)
            .memberId(member.getId())
            .email(member.getEmail())
            .build();
    }

    private void saveRefreshToken(Member member, TokenDTO token) {
        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(member.getId())
            .token(token.getRefreshToken())
            .build();

        // 리프레시 토큰 저장
        refreshTokenRepository.save(refreshToken);
    }


    @Override
    public MemberUpdateDTO updateTheme(Long memberId, MemberThemeDTO requestDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        List<Theme> themes = requestDTO.getThemes().stream()
            .map(themeType -> themeRepository.findByStudyTheme(themeType).orElseThrow(() -> new GeneralException(ErrorStatus._THEME_NOT_FOUND)))
            .toList();

        List<MemberTheme> memberThemes = themes.stream()
            .map(theme -> MemberTheme.builder().member(member).theme(theme).build())
            .toList();

        if (memberThemeRepository.existsByMemberId(member.getId()))
            memberThemeRepository.deleteByMemberId(member.getId());

        // 새로운 MemberTheme과 PreferredRegion을 저장
        memberThemeRepository.saveAll(memberThemes);

        member.updateThemes(memberThemes);

        memberRepository.save(member);

        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    @Override
    public MemberUpdateDTO updateRegion(Long memberId, MemberRegionDTO requestDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        List<Region> regions = requestDTO.getRegions().stream()
            .map(regionCode -> regionRepository.findByCode(regionCode).orElseThrow(() -> new GeneralException(ErrorStatus._REGION_NOT_FOUND)))
            .toList();

        List<PreferredRegion> preferredRegions = regions.stream()
            .map(region -> PreferredRegion.builder().member(member).region(region).build())
            .toList();

        // 기존의 MemberTheme과 PreferredRegion 삭제

        if (preferredRegionRepository.existsByMemberId(member.getId()))
            preferredRegionRepository.deleteByMemberId(member.getId());


        preferredRegionRepository.saveAll(preferredRegions);

        member.updateRegions(preferredRegions);

        memberRepository.save(member);

        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    @Override
    public MemberSignInDTO signUpByKAKAOForTest(String code) throws JsonProcessingException {
        // 카카오 OAuth 서비스에서 액세스 토큰 요청
        ResponseEntity<String> accessTokenResponse = kaKaoOAuthService.requestAccessToken(code);

        // 응답에서 액세스 토큰을 파싱
        KaKaoOAuthTokenDTO oAuthToken = kaKaoOAuthService.getAccessToken(accessTokenResponse);
        System.out.println(oAuthToken.getAccess_token());

        // 액세스 토큰을 사용하여 사용자 정보 요청
        ResponseEntity<String> userInfoResponse = kaKaoOAuthService.requestUserInfo(oAuthToken.getAccess_token());

        // 응답에서 사용자 정보를 파싱
        KaKaoUser kaKaoUser = kaKaoOAuthService.getUserInfo(userInfoResponse);

        // 사용자가 이미 존재하는지 확인
        if (memberRepository.existsByEmail(kaKaoUser.toMember().getEmail())) {
            // 존재하는 경우, 사용자 정보를 가져옴
            Member member = memberRepository.findByEmail(kaKaoUser.toMember().getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

            // JWT 토큰 생성
            TokenDTO token = jwtTokenProvider.createToken(member.getId());

            saveRefreshToken(member, token);

            // 로그인 DTO 반환
            return MemberResponseDTO.MemberSignInDTO.builder()
                .tokens(token)
                .memberId(member.getId())
                .email(member.getEmail())
                .build();
        }

        // 존재하지 않는 경우, 새로운 회원 정보 저장
        Member member = memberRepository.save(kaKaoUser.toMember());

        // JWT 토큰 생성
        TokenDTO token = jwtTokenProvider.createToken(member.getId());

        saveRefreshToken(member, token);

        // 회원 가입 DTO 반환
        return MemberResponseDTO.MemberSignInDTO.builder()
            .tokens(token)
            .memberId(member.getId())
            .email(member.getEmail())
            .build();
    }

    @Override
    public void redirectURL() throws IOException {
        response.sendRedirect(kaKaoOAuthService.getOauthRedirectURL());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long memberId = parseUsernameToMemberId(username);
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + (member.getIsAdmin() ? "ADMIN" : "USER"))
        );

        return CustomUserDetails.builder()
            .email(member.getEmail())
            .memberId(member.getId())
            .password(member.getPassword())
            .enabled(true)
            .authorities(authorities)
            .build();
    }

    @Override
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));
    }

    @Override
    public boolean isMemberExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public MemberUpdateDTO updateProfile(Long memberId, MemberInfoListDTO requestDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));
        member.updateInfo(requestDTO);
        memberRepository.save(member);
        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    @Override
    public MemberUpdateDTO updateStudyReason(Long memberId, MemberReasonDTO requestDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        List<Reason> reasons = requestDTO.getReasons().stream()
            .map(Reason::fromCode)
            .toList();

        List<StudyReason> studyReasons = reasons.stream()
            .map(reason -> StudyReason.builder().member(member).reason(reason).build())
            .toList();

        // 기존의 MemberTheme과 PreferredRegion 삭제

        if (studyReasonRepository.existsByMemberId(member.getId()))
            studyReasonRepository.deleteByMemberId(member.getId());


        studyReasonRepository.saveAll(studyReasons);

        member.updateReasons(studyReasons);

        memberRepository.save(member);

        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    @Override
    public MemberUpdateDTO toAdmin(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));
        member.toAdmin();
        memberRepository.save(member);
        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    @Override
    public MemberTestDTO testMember(TestMemberDTO requestDTO) {
        Random random = new Random();

        String name = "testMember" + random.nextInt(1000);
        String email = "test" + random.nextInt(1000) + "@gmail.com";
        String password = UUID.randomUUID().toString().substring(0, 8);
        String phone = "010" + (random.nextInt(90000000) + 10000000);
        LocalDate birth = LocalDate.of(
            random.nextInt(50) + 1970, // Year between 1970 and 2020
            random.nextInt(12) + 1,    // Month between 1 and 12
            random.nextInt(28) + 1     // Day between 1 and 28 to avoid invalid dates
        );

        Member member = Member.builder()
            .name(name)
            .email(email)
            .password(password)
            .carrier(Carrier.NONE)
            .phone(phone)
            .birth(birth)
            .profileImage("test")
            .personalInfo(true)
            .idInfo(true)
            .isAdmin(false)
            .status(Status.ON)
            .build();

        memberRepository.save(member);

        updateTheme(member.getId(), requestDTO.getThemes());
        updateRegion(member.getId(), requestDTO.getRegions());

        TokenDTO token = jwtTokenProvider.createToken(member.getId());

        saveRefreshToken(member, token);

        return MemberTestDTO.builder()
            .memberId(member.getId())
            .email(member.getEmail())
            .tokens(token)
            .build();
    }


    private Long parseUsernameToMemberId(String username) {
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user ID format");
        }
    }

}
