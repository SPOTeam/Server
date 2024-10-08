package com.example.spot.service.member;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.GeneralException;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.domain.StudyReason;
import com.example.spot.domain.enums.Carrier;
import com.example.spot.domain.enums.LoginType;
import com.example.spot.domain.enums.Reason;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.repository.StudyReasonRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.web.dto.member.MemberRequestDTO;
import com.example.spot.web.dto.member.MemberRequestDTO.MemberReasonDTO;
import com.example.spot.domain.auth.CustomUserDetails;
import com.example.spot.domain.auth.RefreshToken;
import com.example.spot.repository.RefreshTokenRepository;
import com.example.spot.security.utils.JwtTokenProvider;
import com.example.spot.domain.Member;
import com.example.spot.repository.MemberRepository;
import com.example.spot.service.auth.KaKaoOAuthService;
import com.example.spot.web.dto.member.MemberResponseDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberRegionDTO.RegionDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberSignInDTO;
import com.example.spot.web.dto.member.MemberResponseDTO.MemberStudyReasonDTO;
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

    /**
     * 카카오 로그인을 통해 회원 가입 또는 로그인을 수행합니다.
     * @param accessToken 카카오 OAuth 액세스 토큰
     * @return SPOT 서버에서 발급한 JWT 토큰 및 회원 정보
     * @throws JsonProcessingException 카카오 사용자 정보 파싱 중 발생하는 예외
     */
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

    /**
     * 리프레시 토큰을 DB에 저장합니다.
     * @param member 리프레시 토큰을 발급한 회원 정보
     * @param token 발급된 토큰 정보
     */
    private void saveRefreshToken(Member member, TokenDTO token) {
        // 기존 리프레시 토큰 삭제
        if (refreshTokenRepository.existsByMemberId(member.getId()))
            refreshTokenRepository.deleteAllByMemberId(member.getId());

        // DB에 저장하기 위한 새로운 리프레시 토큰 객체 생성
        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(member.getId())
            .token(token.getRefreshToken())
            .build();

        // 리프레시 토큰 저장
        refreshTokenRepository.save(refreshToken);
    }


    /**
     * 회원의 테마 정보를 업데이트합니다.
     * @param memberId 회원 ID
     * @param requestDTO 업데이트할 테마 정보
     * @return 업데이트된 회원 정보와 업데이트 시간
     * @throws MemberHandler 회원이 존재하지 않을 경우
     * @throws GeneralException 테마가 존재하지 않을 경우
     */
    @Override
    public MemberUpdateDTO updateTheme(Long memberId, MemberThemeDTO requestDTO) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 테마 정보 조회
        List<Theme> themes = requestDTO.getThemes().stream()
            .map(themeType -> themeRepository.findByStudyTheme(themeType).orElseThrow(() -> new GeneralException(ErrorStatus._THEME_NOT_FOUND)))
            .toList();

        // MemberTheme 객체 생성
        List<MemberTheme> memberThemes = themes.stream()
            .map(theme -> MemberTheme.builder().member(member).theme(theme).build())
            .toList();

        // 기존의 MemberTheme 삭제
        if (memberThemeRepository.existsByMemberId(member.getId()))
            memberThemeRepository.deleteByMemberId(member.getId());

        // 새로운 MemberTheme과 PreferredRegion을 저장
        memberThemeRepository.saveAll(memberThemes);

        // 회원 정보 업데이트
        member.updateThemes(memberThemes);

        // 회원 정보 저장
        memberRepository.save(member);

        // 업데이트된 회원 정보 반환
        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    /**
     * 회원의 지역 정보를 업데이트합니다.
     * @param memberId 회원 ID
     * @param requestDTO 업데이트할 지역 정보
     * @return 업데이트된 회원 정보와 업데이트 시간
     * @throws MemberHandler 회원이 존재하지 않을 경우
     * @throws GeneralException 지역이 존재하지 않을 경우
     *
     */
    @Override
    public MemberUpdateDTO updateRegion(Long memberId, MemberRegionDTO requestDTO) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 지역 정보 조회
        List<Region> regions = requestDTO.getRegions().stream()
            .map(regionCode -> regionRepository.findByCode(regionCode).orElseThrow(() -> new GeneralException(ErrorStatus._REGION_NOT_FOUND)))
            .toList();

        // PreferredRegion 객체 생성
        List<PreferredRegion> preferredRegions = regions.stream()
            .map(region -> PreferredRegion.builder().member(member).region(region).build())
            .toList();

        // 기존의 MemberTheme과 PreferredRegion 삭제
        if (preferredRegionRepository.existsByMemberId(member.getId()))
            preferredRegionRepository.deleteByMemberId(member.getId());

        // 새로운 PreferredRegion을 저장
        preferredRegionRepository.saveAll(preferredRegions);

        // 회원 정보 업데이트
        member.updateRegions(preferredRegions);

        // 회원 정보 저장
        memberRepository.save(member);

        // 업데이트된 회원 정보 반환
        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    /**
     * 카카오 로그인을 테스트용으로 수행합니다. 카카오 auth accessToken 발급을 포함한 모든 내부 로직이 구현 되어 있습니다.
     * @param code 카카오 로그인 요청 시 발급받은 코드
     * @return SPOT 서버에서 발급한 JWT 토큰 및 회원 정보
     * @throws JsonProcessingException 카카오 사용자 정보 파싱 중 발생하는 예외
     * @throws MemberHandler 이메일로 가입된 내역이 존재하지만, 실제로는 회원이 존재하지 않을 경우
     */
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
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

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

    /**
     * redirectURL을 반환합니다.
     * @throws IOException URL 리다이렉트 중 발생하는 예외
     */
    @Override
    public void redirectURL() throws IOException {
        // 카카오 OAuth 서비스에서 리다이렉트 URL 반환
        response.sendRedirect(kaKaoOAuthService.getOauthRedirectURL());
    }

    /**
     * 회원의 정보를 조회합니다.
     * @param username 회원 식별자(ID)
     * @return 회원 정보
     * @throws UsernameNotFoundException 회원을 찾을 수 없을 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 회원 ID Long 타입으로 변환
        Long memberId = parseUsernameToMemberId(username);

        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 권한 설정 -> ROLE_USER 또는 ROLE_ADMIN
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + (member.getIsAdmin() ? "ADMIN" : "USER"))
        );

        // CustomUserDetails 객체 생성
        return CustomUserDetails.builder()
            .email(member.getEmail())
            .memberId(member.getId())
            .password(member.getPassword())
            .enabled(true)
            .authorities(authorities)
            .build();
    }

    /**
     * 회원의 이메일로 회원을 조회합니다.
     * @param email 회원 이메일
     * @return 회원 객체
     * @throws MemberHandler 회원을 찾을 수 없을 경우
     */
    @Override
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
    }

    /**
     * 회원의 존재 여부를 확인합니다.
     * @param email 회원 이메일
     * @return 회원 존재 여부
     */
    @Override
    public boolean isMemberExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 회원의 프로필을 업데이트 합니다.
     * @param memberId 변경할 회원 ID
     * @param requestDTO 변경할 회원 정보
     * @return 변경 된 회원 ID와 변경 시간
     * @throws MemberHandler 회원을 찾을 수 없을 경우
     */
    @Override
    public MemberUpdateDTO updateProfile(Long memberId, MemberRequestDTO.MemberUpdateDTO requestDTO) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 회원 정보 업데이트
        member.updateInfo(requestDTO);

        // 회원 정보 저장
        memberRepository.save(member);

        // 업데이트된 회원 정보 반환
        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    /**
     * 회원의 스터디 참여 이유를 변경합니다.
     * @param memberId 변경할 회원 ID
     * @param requestDTO 변경할 이유 정보
     * @return 변경 된 회원 ID와 변경 시간
     * @throws MemberHandler 회원을 찾을 수 없을 경우
     */
    @Override
    public MemberUpdateDTO updateStudyReason(Long memberId, MemberReasonDTO requestDTO) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 이유 정보 조회
        List<Reason> reasons = requestDTO.getReasons().stream()
            .map(Reason::fromCode)
            .toList();

        // StudyReason 객체 생성
        List<StudyReason> studyReasons = reasons.stream()
            .map(reason -> StudyReason.builder().member(member).reason(reason.getCode()).build())
            .toList();

        // 기존의 StudyReason 삭제
        if (studyReasonRepository.existsByMemberId(member.getId()))
            studyReasonRepository.deleteByMemberId(member.getId());

        // 새로운 StudyReason 저장
        studyReasonRepository.saveAll(studyReasons);

        // 회원 정보 업데이트
        member.updateReasons(studyReasons);

        // 회원 정보 저장
        memberRepository.save(member);

        // 업데이트된 회원 정보 반환
        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    /**
     * 회원의 테마 정보를 조회합니다.
     * @param memberId 조회할 회원 ID
     * @return 회원의 테마 정보 및 회원 ID
     * @throws MemberHandler 회원을 찾을 수 없을 경우
     * @throws MemberHandler 회원 테마 정보를 찾을 수 없을 경우
     */
    @Override
    public MemberResponseDTO.MemberThemeDTO getThemes(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        if (member.getMemberThemeList().isEmpty())
            throw new MemberHandler(ErrorStatus._MEMBER_THEME_NOT_FOUND);

        List<Theme> themes = member.getMemberThemeList().stream()
            .map(MemberTheme::getTheme)
            .toList();

        List<ThemeType> themeTypes = themes.stream()
            .map(Theme::getStudyTheme)
            .toList();

        return MemberResponseDTO.MemberThemeDTO.builder()
            .memberId(member.getId())
            .themes(themeTypes)
            .build();
    }


    /**
     * 회원의 지역 정보를 조회합니다.
     * @param memberId 조회할 회원 ID
     * @return 회원의 지역 정보 및 회원 ID
     * @throws MemberHandler 회원을 찾을 수 없을 경우
     * @throws MemberHandler 회원 지역 정보를 찾을 수 없을 경우
     */
    @Override
    public MemberResponseDTO.MemberRegionDTO getRegions(Long memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 회원의 지역 정보가 없을 경우
        if (member.getRegions().isEmpty())
            throw new MemberHandler(ErrorStatus._MEMBER_REGION_NOT_FOUND);

        // 회원의 지역 정보 조회
        List<Region> regions = member.getPreferredRegionList().stream()
            .map(PreferredRegion::getRegion)
            .toList();

        // 지역 정보 DTO로 변환
        List<RegionDTO> codes = regions.stream()
            .map(region -> RegionDTO.builder()
                .province(region.getProvince())
                .district(region.getDistrict())
                .neighborhood(region.getNeighborhood())
                .code(region.getCode())
                .build())
            .toList();

        // 회원의 지역 정보 반환
        return MemberResponseDTO.MemberRegionDTO.builder()
            .memberId(member.getId())
            .regions(codes)
            .build();
    }

    /**
     * 회원의 스터디 참여 이유를 조회합니다.
     * @param memberId 조회할 회원 ID
     * @return 회원의 스터디 참여 이유 및 회원 ID
     * @throws MemberHandler 회원을 찾을 수 없을 경우
     * @throws MemberHandler 회원 스터디 참여 이유를 찾을 수 없을 경우
     */
    @Override
    public MemberStudyReasonDTO getStudyReasons(Long memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 회원의 스터디 참여 이유가 없을 경우
        if (member.getStudyReasonList().isEmpty())
            throw new MemberHandler(ErrorStatus._MEMBER_STUDY_REASON_NOT_FOUND);

        // 회원의 스터디 참여 이유 ID 조회
        List<Long> reasonNums = member.getStudyReasonList().stream()
            .map(StudyReason::getReason)
            .toList();

        // 이유 ID를 이유 객체로 변환
        List<Reason> reasons = reasonNums.stream()
            .map(Reason::fromCode)
            .toList();

        // 회원의 스터디 참여 이유 반환
        return MemberStudyReasonDTO.builder()
            .memberId(member.getId())
            .reasons(reasons)
            .build();
    }

    /**
     * 회원에게 관리자 권한을 부여합니다.
     * @param memberId 관리자로 변경할 회원 ID
     * @return 변경 된 회원 ID와 변경 시간
     * @throws MemberHandler 회원을 찾을 수 없을 경우
     */
    @Override
    public MemberUpdateDTO toAdmin(Long memberId) {
        // 회원 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 관리자 권한 부여
        member.toAdmin();

        // 회원 정보 저장
        memberRepository.save(member);

        // 변경 된 회원 정보 반환
        return MemberUpdateDTO.builder()
            .memberId(member.getId())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    /**
     * 테스트 회원을 생성합니다.
     * @param memberInfoListDTO 생성할 회원 정보
     * @return 생성된 회원 개인 정보와 토큰
     */
    @Override
    public MemberTestDTO testMember(MemberInfoListDTO memberInfoListDTO) {

        // 회원 생성
        Member member = Member.builder()
                .name(memberInfoListDTO.getName())
                .carrier(memberInfoListDTO.getCarrier())
                .birth(memberInfoListDTO.getBirth())
                .nickname(memberInfoListDTO.getNickname())
                .email(memberInfoListDTO.getEmail())
                .password(UUID.randomUUID().toString())
                .phone(memberInfoListDTO.getPhone())
                .personalInfo(memberInfoListDTO.isPersonalInfo())
                .idInfo(memberInfoListDTO.isIdInfo())
                .profileImage(memberInfoListDTO.getProfileImage())
                .status(Status.ON)
                .loginType(LoginType.NORMAL)
                .build();

        // 회원 저장
        memberRepository.save(member);

        // 테마 정보 저장
        updateTheme(member.getId(), memberInfoListDTO.getThemes());
        // 지역 정보 저장
        updateRegion(member.getId(), memberInfoListDTO.getRegions());

        // 토큰 생성
        TokenDTO token = jwtTokenProvider.createToken(member.getId());

        // 리프레시 토큰 저장
        saveRefreshToken(member, token);

        // 회원 정보 반환
        return MemberTestDTO.builder()
            .memberId(member.getId())
            .email(member.getEmail())
            .tokens(token)
            .build();
    }


    /**
     * 문자열로 입력된 회원 ID를 Long 타입으로 파싱합니다.
     * @param username 회원 ID 문자열
     * @return 회원 ID
     * @throws UsernameNotFoundException 회원 ID 형식이 잘못된 경우
     */
    private Long parseUsernameToMemberId(String username) {
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user ID format");
        }
    }

}
