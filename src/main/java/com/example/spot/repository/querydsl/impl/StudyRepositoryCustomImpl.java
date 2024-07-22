package com.example.spot.repository.querydsl.impl;

import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.QStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.querydsl.StudyRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

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

    @Override
    public List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndThemeTypes(
        Map<String, Object> search, StudySortBy sortBy, Pageable pageable, List<StudyTheme> themes) {
        QStudy study = QStudy.study;

        BooleanBuilder builder = new BooleanBuilder();

        // 조건문 추가
        if (search.get("isOnline") != null) {
            builder.and(study.isOnline.eq((Boolean) search.get("isOnline")));
        }
        if (search.get("gender") != null) {
            builder.and(study.gender.eq((Gender) search.get("gender")));
        }
        if (search.get("minAge") != null) {
            builder.and(study.minAge.goe((Integer) search.get("minAge")));
        }
        if (search.get("maxAge") != null) {
            builder.and(study.maxAge.loe((Integer) search.get("maxAge")));
        }
        if (search.get("hasFee") != null) {
            builder.and(study.hasFee.eq((Boolean) search.get("hasFee")));
        }
        if (search.get("fee") != null) {
            builder.and(study.fee.loe((Integer) search.get("fee")));
        }
        if (themes != null && !themes.isEmpty()) {
            builder.and(study.themes.any().in(themes));
        }
        if (sortBy != null && sortBy.equals(StudySortBy.RECRUITING)){
            builder.and(study.studyState.eq((StudyState.RECRUITING)));
        }

        // 정렬 조건 설정
        JPAQuery<Study> query = queryFactory.selectFrom(study)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());


        switch (sortBy) {
            case HIT:
                query.orderBy(study.hitNum.desc());
                query.orderBy(study.createdAt.desc());
                break;
            case LIKED:
                query.orderBy(study.heartCount.desc());
                query.orderBy(study.createdAt.desc());
                break;
            case RECRUITING:
                query.orderBy(study.createdAt.desc());
                break;
            default:
                query.orderBy(study.createdAt.desc());
                break;
        }

        return query.fetch();
    }


    @Override
    public List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndRegionCodes(
        Map<String, Object> search, StudySortBy sortBy, Pageable pageable,
        List<String> regionCodes) {
        return null;
    }

    @Override
    public List<Study> findStudyByGenderAndAgeAndIsOnlineAndHasFeeAndFeeAndRegionCode(
        Map<String, Object> search, StudySortBy sortBy, Pageable pageable, String regionCode) {
        return null;
    }
}
