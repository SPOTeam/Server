package com.example.spot.service.study;


import com.example.spot.domain.enums.ThemeType;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchStudyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudyQueryServiceImpl implements StudyQueryService {

    private final StudyRepository studyRepository;
    private final MemberStudyRepository memberStudyRepository;

    @Override
    public Page<SearchStudyDTO> findRecommendStudies(Pageable pageable, Long memberId) {
        return null;
    }

    @Override
    public Page<SearchStudyDTO> findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<SearchStudyDTO> findInterestStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchStudyDTO request, ThemeType theme) {
        return null;
    }

    @Override
    public Page<SearchStudyDTO> findInterestRegionStudiesByConditionsAll(Pageable pageable,
        Long memberId, SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<SearchStudyDTO> findInterestRegionStudiesByConditionsSpecific(Pageable pageable,
        Long memberId, SearchStudyDTO request, String regionCode) {
        return null;
    }

    @Override
    public Page<SearchStudyDTO> findRecruitingStudiesByConditions(Pageable pageable,
        SearchStudyDTO request) {
        return null;
    }

    @Override
    public Page<SearchStudyDTO> findLikedStudiesByConditions(Pageable pageable, Long memberId) {
        return null;
    }
}
