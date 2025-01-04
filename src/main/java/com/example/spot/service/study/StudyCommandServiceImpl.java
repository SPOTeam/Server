package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.StudyLikeStatus;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.PreferredStudy;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.*;
import com.example.spot.security.utils.SecurityUtils;
import com.example.spot.web.dto.study.request.StudyJoinRequestDTO;
import com.example.spot.web.dto.study.request.StudyRegisterRequestDTO;
import com.example.spot.web.dto.study.response.StudyJoinResponseDTO;
import com.example.spot.web.dto.study.response.StudyLikeResponseDTO;
import com.example.spot.web.dto.study.response.StudyRegisterResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StudyCommandServiceImpl implements StudyCommandService {


    @Value("${study.keyword}")
    private String KEYWORD;

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final RegionRepository regionRepository;
    private final ThemeRepository themeRepository;

    private final MemberStudyRepository memberStudyRepository;
    private final RegionStudyRepository regionStudyRepository;
    private final StudyThemeRepository studyThemeRepository;
    private final PreferredStudyRepository preferredStudyRepository;

    private final RedisTemplate<String, String> redisTemplate;

    /* ----------------------------- 스터디 생성/참여 관련 API ------------------------------------- */

    // [스터디 생성/참여] 참여 신청하기
    @Transactional
    public StudyJoinResponseDTO.JoinDTO applyToStudy(Long studyId, StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO) {

        // Authorization
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 모집중이지 않은 스터디에 신청할 수 없음
        if (study.getStudyState() != StudyState.RECRUITING) {
            throw new StudyHandler(ErrorStatus._STUDY_NOT_RECRUITING);
        }

        if (study.getMaxPeople() <= memberStudyRepository.countByStatusAndStudyId(ApplicationStatus.APPROVED, studyId))
            throw new StudyHandler(ErrorStatus._STUDY_IS_FULL);


        // 이미 신청한 스터디에 다시 신청할 수 없음
        List<MemberStudy> memberStudyList = memberStudyRepository.findByMemberIdAndStatusNot(memberId, ApplicationStatus.REJECTED).stream()
                .filter(memberStudy -> study.equals(memberStudy.getStudy()))
                .toList();

        // memberStudy에 내가 소유한 스터디가 있으면 에러 발생
        if (memberStudyList.stream().anyMatch(MemberStudy::getIsOwned)) {
            throw new StudyHandler(ErrorStatus._STUDY_OWNER_CANNOT_APPLY);
        }

        if (!memberStudyList.isEmpty()) {
            throw new StudyHandler(ErrorStatus._STUDY_ALREADY_APPLIED);
        }

        MemberStudy memberStudy = MemberStudy.builder()
                .isOwned(false)
                .introduction(studyJoinRequestDTO.getIntroduction())
                .member(member)
                .study(study)
                .status(ApplicationStatus.APPLIED)
                .build();

        member.addMemberStudy(memberStudy);
        study.addMemberStudy(memberStudy);
        memberStudyRepository.save(memberStudy);

        return StudyJoinResponseDTO.JoinDTO.toDTO(member, study);
    }

    // [스터디 생성/참여] 스터디 생성하기
    @Transactional
    public StudyRegisterResponseDTO.RegisterDTO registerStudy(StudyRegisterRequestDTO.RegisterDTO studyRegisterRequestDTO) {

        // Authorization
        Long memberId = SecurityUtils.getCurrentUserId();
        SecurityUtils.verifyUserId(memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Study study = Study.builder()
                .gender(studyRegisterRequestDTO.getGender())
                .minAge(studyRegisterRequestDTO.getMinAge())
                .maxAge(studyRegisterRequestDTO.getMaxAge())
                .fee(studyRegisterRequestDTO.getFee())
                .profileImage(studyRegisterRequestDTO.getProfileImage())
                .isOnline(studyRegisterRequestDTO.getIsOnline())
                .hasFee(studyRegisterRequestDTO.isHasFee())
                .goal(studyRegisterRequestDTO.getGoal())
                .introduction(studyRegisterRequestDTO.getIntroduction())
                .title(studyRegisterRequestDTO.getTitle())
                .maxPeople(studyRegisterRequestDTO.getMaxPeople())
                .build();

        study = studyRepository.save(study);

        createMemberStudy(member, study);
        createRegionStudy(study, studyRegisterRequestDTO);
        createStudyTheme(study, studyRegisterRequestDTO);

        studyRepository.save(study);

        return StudyRegisterResponseDTO.RegisterDTO.toDTO(study);
    }

    /**
     * 특정 스터디에 좋아요를 누르거나 취소합니다. 이미 좋아요가 눌려있다면 취소하고, 아니라면 좋아요를 누릅니다.
     * @param memberId 회원 ID
     * @param studyId 스터디 ID
     * @return 스터디 제목과 좋아요 상태를 반환합니다.
     * @throws StudyHandler 스터디가 존재하지 않는 경우
     * @throws MemberHandler 회원이 존재하지 않는 경우
     * @see StudyLikeResponseDTO
     */
    @Override
    public StudyLikeResponseDTO likeStudy(Long memberId, Long studyId) {

        // 회원과 스터디 조회
        Study study = studyRepository.findById(studyId)
            .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        // 현재 좋아요 상태 확인 -> 만약 없다면, 객체 하나 생성
        PreferredStudy preferredStudy = preferredStudyRepository
            .findByMemberIdAndStudyId(memberId, studyId)
            .orElse(PreferredStudy.builder()
                .member(member)
                .study(study)
                .studyLikeStatus(StudyLikeStatus.DISLIKE)
                .build());

        // 상태에 따라 변경
        if (preferredStudy.getStudyLikeStatus() == StudyLikeStatus.LIKE) {
            preferredStudy.changeStatus(StudyLikeStatus.DISLIKE);
            study.deletePreferredStudy(preferredStudy);
        } else {
            preferredStudy.changeStatus(StudyLikeStatus.LIKE);
            study.addPreferredStudy(preferredStudy);
        }
        // 저장 및 응답 객체 생성
        preferredStudyRepository.save(preferredStudy);
        return new StudyLikeResponseDTO(preferredStudy);
    }


    private void createMemberStudy(Member member, Study study) {

        MemberStudy memberStudy = MemberStudy.builder()
                .isOwned(true)
                .introduction(study.getIntroduction())
                .member(member)
                .study(study)
                .status(ApplicationStatus.APPROVED)
                .build();

        member.addMemberStudy(memberStudy);
        study.addMemberStudy(memberStudy);
        memberStudyRepository.save(memberStudy);

        study.addMemberStudy(memberStudy);

    }

    private void createRegionStudy(Study study, StudyRegisterRequestDTO.RegisterDTO studyRegisterRequestDTO) {

        studyRegisterRequestDTO.getRegions()
                .forEach(stringRegion -> {

                    Region region = regionRepository
                            .findByCode(stringRegion)
                            .orElseThrow(() -> new StudyHandler(ErrorStatus._REGION_NOT_FOUND));

                    RegionStudy regionStudy = RegionStudy.builder()
                            .region(region)
                            .study(study)
                            .build();

                    region.addRegionStudy(regionStudy);
                    study.addRegionStudy(regionStudy);
                    regionStudyRepository.save(regionStudy);

                    study.addRegionStudy(regionStudy);
                });
    }

    private void createStudyTheme(Study study, StudyRegisterRequestDTO.RegisterDTO studyRegisterRequestDTO) {

        studyRegisterRequestDTO.getThemes()
                .forEach(stringTheme -> {

                    Theme theme = themeRepository.findByStudyTheme(stringTheme)
                            .orElseThrow(() -> new StudyHandler(ErrorStatus._THEME_NOT_FOUND));

                    StudyTheme studyTheme = StudyTheme.builder()
                            .theme(theme)
                            .study(study)
                            .build();

                    study.addStudyTheme(studyTheme);
                    theme.addStudyTheme(studyTheme);
                    studyThemeRepository.save(studyTheme);

                    study.addStudyTheme(studyTheme);
                });
    }

    /* ---------------------------------- 인기 검색어 --------------------------------------------- */

    /**
     * 검색어를 인기 검색어(Redis)에 추가합니다. 이미 존재하는 검색어라면 score를 1 증가시킵니다.
     * @param keyword 검색어
     */
    @Override
    public void addHotKeyword(String keyword) {
       redisTemplate.opsForZSet().incrementScore(KEYWORD, keyword, 1);
    }
}
