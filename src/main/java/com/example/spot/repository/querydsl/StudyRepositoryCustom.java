package com.example.spot.repository.querydsl;

import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.study.Study;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyRepositoryCustom {
    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFee(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable);

}
