package com.example.spot.repository.querydsl;

import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRepositoryCustom {

    List<Study> findByStudyTheme(List<StudyTheme> studyThemes);

    List<Study> findByStudyThemeAndNotInIds(List<StudyTheme> studyThemes, List<Long> studyIds);

    // 모집중 스터디 조회
    List<Study> findStudyByConditions(Map<String, Object> search, StudySortBy sortBy, Pageable pageable);

    List<Study> findStudyByConditionsAndThemeTypes(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<StudyTheme> themeTypes);

    List<Study> findStudyByConditionsAndRegionStudies(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<RegionStudy> regionStudies);

    List<Study> findAllByTitleContaining(String title, StudySortBy sortBy, Pageable pageable);

    List<Study> findByStudyTheme(List<StudyTheme> studyTheme, StudySortBy sortBy, Pageable pageable);

    List<Study> findByMemberStudy(List<MemberStudy> memberStudy, Pageable pageable);
    List<Study> findRecruitingStudiesByMemberStudy(List<MemberStudy> memberStudy, Pageable pageable);

    long countStudyByConditionsAndThemeTypes(Map<String, Object> search, List<StudyTheme> themeTypes, StudySortBy sortBy);

    long countStudyByConditionsAndRegionStudies(Map<String, Object> search, List<RegionStudy> regionStudies, StudySortBy sortBy);

    long countStudyByConditions(Map<String, Object> search, StudySortBy sortBy);
    long countStudyByStudyTheme(List<StudyTheme> studyThemes, StudySortBy sortBy);

    long countAllByTitleContaining(String title, StudySortBy sortBy);

}
