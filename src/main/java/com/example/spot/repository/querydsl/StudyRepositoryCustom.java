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

    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<StudyTheme> themeTypes);

    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndRegionStudies(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<RegionStudy> regionStudies);

    long countStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(Map<String, Object> search, List<StudyTheme> themeTypes);

    long countStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndRegionStudies(Map<String, Object> search, List<RegionStudy> regionStudies);
}
