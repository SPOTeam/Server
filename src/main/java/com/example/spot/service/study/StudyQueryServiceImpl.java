package com.example.spot.service.study;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.StudyHandler;
import com.example.spot.domain.Member;
import com.example.spot.domain.Theme;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchStudyDTO;
import com.example.spot.web.dto.study.response.StudyInfoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyQueryServiceImpl implements StudyQueryService {

    private final MemberRepository memberRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final StudyRepository studyRepository;


    @Override
    public Page<Study> findRecommendStudies(Pageable pageable, Long memberId) {
        return null;
    }

    @Override
    public Page<Study> findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<Study> findInterestStudiesByConditionsSpecific(Pageable pageable, Long memberId,
        SearchStudyDTO request, Theme theme) {
        return null;
    }


    @Override
    public Page<Study> findInterestRegionStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<Study> findInterestRegionStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchStudyDTO request, String regionCode) {
        return null;
    }

    @Override
    public Page<Study> findRecruitingStudiesByConditions(Pageable pageable,
        SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<Study> findLikedStudiesByConditions(Pageable pageable, Long memberId) {
        return null;
    }
}
