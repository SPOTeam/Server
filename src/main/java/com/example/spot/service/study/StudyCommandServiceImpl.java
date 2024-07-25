package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Region;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.*;
import com.example.spot.web.dto.study.request.StudyJoinRequestDTO;
import com.example.spot.web.dto.study.request.StudyRegisterRequestDTO;
import com.example.spot.web.dto.study.response.StudyJoinResponseDTO;
import com.example.spot.web.dto.study.response.StudyRegisterResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyCommandServiceImpl implements StudyCommandService {

    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final RegionRepository regionRepository;
    private final ThemeRepository themeRepository;

    private final MemberStudyRepository memberStudyRepository;
    private final RegionStudyRepository regionStudyRepository;
    private final StudyThemeRepository studyThemeRepository;

    /* ----------------------------- 스터디 생성/참여 관련 API ------------------------------------- */

    // [스터디 생성/참여] 참여 신청하기
    @Transactional
    public StudyJoinResponseDTO.JoinDTO applyToStudy(Long memberId, Long studyId,
                                                     StudyJoinRequestDTO.StudyJoinDTO studyJoinRequestDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new StudyHandler(ErrorStatus._STUDY_NOT_FOUND));

        // 모집중이지 않은 스터디에 신청할 수 없음
        if (study.getStudyState() != StudyState.RECRUITING) {
            throw new StudyHandler(ErrorStatus._STUDY_NOT_RECRUITING);
        }

        // 이미 신청한 스터디에 다시 신청할 수 없음
        List<MemberStudy> memberStudyList = memberStudyRepository.findByMemberId(memberId).stream()
                .filter(memberStudy -> study.equals(memberStudy.getStudy()))
                .toList();

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
    public StudyRegisterResponseDTO.RegisterDTO registerStudy(Long memberId, StudyRegisterRequestDTO.RegisterDTO studyRegisterRequestDTO) {

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
                            .findByProvinceAndDistrictAndNeighborhood(stringRegion.getProvince(), stringRegion.getDistrict(), stringRegion.getNeighborhood())
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
}
