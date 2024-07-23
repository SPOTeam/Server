package com.example.spot.repository.querydsl;

import com.example.spot.domain.study.Study;
import java.util.List;
import java.util.Map;

public interface StudyRepositoryCustom {
    List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFee(Map<String, Object> search);

}
