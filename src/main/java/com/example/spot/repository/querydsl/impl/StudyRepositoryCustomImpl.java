package com.example.spot.repository.querydsl.impl;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.querydsl.StudyRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import static com.example.spot.domain.study.QStudy.study;
@RequiredArgsConstructor
public class StudyRepositoryCustomImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFee(Map<String, Object> search,
        StudySortBy sortBy, org.springframework.data.domain.Pageable pageable) {
        // 코드 작성
        return queryFactory
            .selectFrom(study)
            .where(study.isOnline.eq((Boolean) search.get("isOnline"))
                .and(study.gender.eq((Gender) search.get("gender"))
                    .and(study.maxAge.loe((Integer) search.get("maxAge"))
                        .and(study.minAge.goe((Integer) search.get("minAge"))
                            .and(study.hasFee.eq((Boolean) search.get("hasFee"))
                                .and(study.fee.loe((Integer) search.get("fee"))))))))
            .orderBy(
                sortBy == StudySortBy.HIT ? study.hitNum.desc() :
                    sortBy == StudySortBy.LIKED ? study.heartCount.desc() :
                        sortBy == StudySortBy.RECRUITING ? study.createdAt.asc() :
                            study.id.asc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    }
}
