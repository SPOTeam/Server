package com.example.spot.repository.querydsl;

import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyRepositoryCustom {

    List<Study> findByStudyTheme(List<StudyTheme> studyThemes);


    // 모집중 스터디 조회
    List<Study> findStudyByConditions(Map<String, Object> search, StudySortBy sortBy, Pageable pageable);

    List<Study> findStudyByConditionsAndThemeTypes(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<StudyTheme> themeTypes);

    List<Study> findStudyByConditionsAndRegionStudies(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<RegionStudy> regionStudies);

    List<Study> findAllByTitleContaining(String title, StudySortBy sortBy, Pageable pageable);

    List<Study> findByStudyTheme(List<StudyTheme> studyTheme, StudySortBy sortBy, Pageable pageable);

    long countStudyByConditionsAndThemeTypes(Map<String, Object> search, List<StudyTheme> themeTypes);

    long countStudyByConditionsAndRegionStudies(Map<String, Object> search, List<RegionStudy> regionStudies);

    long countStudyByConditions(Map<String, Object> search);
    long countStudyByStudyTheme(List<StudyTheme> studyThemes);

    long countAllByTitleContaining(String title);

}
