package com.example.spot.repository.querydsl;

import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.Study;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyRepositoryCustom {
    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFee(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable);

    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<StudyTheme> themeTypes);

    long countStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(Map<String, Object> search, List<StudyTheme> themeTypes);

    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndRegionCodes(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<String> regionCodes);

    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndRegionCode(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, String regionCode);
}
