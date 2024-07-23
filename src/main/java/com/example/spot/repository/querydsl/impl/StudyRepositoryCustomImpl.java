package com.example.spot.repository.querydsl.impl;

import com.example.spot.domain.study.QStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.querydsl.StudyRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudyRepositoryCustomImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFee(
        Map<String, Object> search) {
        // 코드 작성


        return null;
    }
}
