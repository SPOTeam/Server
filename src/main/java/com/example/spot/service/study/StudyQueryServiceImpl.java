package com.example.spot.service.study;

import com.example.spot.config.EntitySpecification;
import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberTheme;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.MemberRepository;
import com.example.spot.repository.MemberStudyRepository;
import com.example.spot.repository.MemberThemeRepository;
import com.example.spot.repository.StudyRepository;
import com.example.spot.web.dto.search.SearchRequestDTO.SearchStudyDTO;
import com.example.spot.web.dto.search.SearchResponseDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyQueryServiceImpl implements StudyQueryService {

    private final StudyRepository studyRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final MemberThemeRepository memberThemeRepository;
    private final MemberRepository memberRepository;

    @Override
    public Page<SearchStudyDTO> findRecommendStudies(Pageable pageable, Long memberId) {
        return null;
    }

    @Override
    public List<SearchResponseDTO.SearchStudyDTO> findInterestStudiesByConditionsAll(Pageable pageable, Long memberId,
        SearchStudyDTO request) {

        // 회원의 테마를 가져오기
        List<Theme> themes = memberThemeRepository.findAllByMemberId(memberId).stream()
            .map(MemberTheme::getTheme)
            .toList();

        // 검색 조건 맵 생성
        Map<String, Object> search = new HashMap<>();
        if (request.getGender() != null)
            search.put("gender", request.getGender());
        if (request.getMinAge() != null)
            search.put("minAge", request.getMinAge());
        if (request.getMaxAge() != null)
            search.put("maxAge", request.getMaxAge());
        if (request.getIsOnline() != null)
            search.put("isOnline", request.getIsOnline());
        if (request.getHasFee() != null)
            search.put("hasFee", request.getHasFee());
        if (request.getFee() != null)
            search.put("fee", request.getFee());

        // request log

        List<Study> studies = studyRepository.findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFee(
            search, request.getSortBy(),
            pageable.getPageNumber(), pageable.getPageSize());

        // 검색 결과를 DTO로 변환

        return studies.stream()
            .map(study -> SearchResponseDTO.SearchStudyDTO.builder()
                .studyId(study.getId())
                .title(study.getTitle())
                .imageUrl(study.getProfileImage())
                .introduction(study.getIntroduction())
                .hitNum(study.getHitNum())
                .memberCount((long) study.getMemberStudies().size())
                .heartCount((long) study.getHeartCount()).build())
            .toList();
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
