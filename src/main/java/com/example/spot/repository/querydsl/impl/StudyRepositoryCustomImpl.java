package com.example.spot.repository.querydsl.impl;

import com.example.spot.domain.enums.Gender;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.StudySortBy;
import com.example.spot.domain.enums.StudyState;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberStudy;
import com.example.spot.domain.mapping.QMemberStudy;
import com.example.spot.domain.mapping.QRegionStudy;
import com.example.spot.domain.mapping.QStudyTheme;
import com.example.spot.domain.mapping.RegionStudy;
import com.example.spot.domain.mapping.StudyTheme;
import com.example.spot.domain.study.QStudy;
import com.example.spot.domain.study.Study;
import com.example.spot.repository.querydsl.StudyRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.example.spot.domain.study.QStudy.study;
@RequiredArgsConstructor
@Slf4j
public class StudyRepositoryCustomImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public List<Study> findAllStudyByConditions(Map<String, Object> search, StudySortBy sortBy,
        Pageable pageable) {
        QStudy study = QStudy.study;
        BooleanBuilder builder = new BooleanBuilder();

        getConditions(search, study, builder);
        getStudyState(sortBy, builder, study);

        JPAQuery<Study> query = queryFactory.selectFrom(study)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        getSortBy(sortBy, query, study);

        return query.fetch();
    }

    @Override
    public List<Study> findAllStudy(StudySortBy sortBy, Pageable pageable) {
        QStudy study = QStudy.study;
        BooleanBuilder builder = new BooleanBuilder();
        getStudyState(sortBy, builder, study);

        JPAQuery<Study> query = queryFactory.selectFrom(study)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        getSortBy(sortBy, query, study);

        return query.fetch();
    }

    @Override
    public List<Study> findByStudyTheme(List<StudyTheme> studyThemes) {
        return queryFactory.selectFrom(study)
            .where(study.studyThemes.any().in(studyThemes))
            .orderBy(study.createdAt.desc())
            .offset(0)
            .limit(3)
            .fetch();
    }

    @Override
    public List<Study> findByStudyThemeAndNotInIds(List<StudyTheme> studyThemes,
        List<Long> studyIds) {
        return queryFactory.selectFrom(study)
            .where(study.studyThemes.any().in(studyThemes))
            .where(study.id.notIn(studyIds))
            .orderBy(study.hitNum.desc())
            .offset(0)
            .limit(3)
            .fetch();
    }

    @Override
    public List<Study> findByRegionStudyAndNotInIds(List<RegionStudy> regionStudies, List<Long> studyIds) {
        return queryFactory.selectFrom(study)
            .where(study.regionStudies.any().in(regionStudies))
            .where(study.id.notIn(studyIds))
            .orderBy(study.heartCount.desc())
            .offset(0)
            .limit(3)
            .fetch();
    }

    @Override
    public List<Study> findRecruitingStudyByConditions(Map<String, Object> search, StudySortBy sortBy,
        Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        getConditions(search, study, builder);
        builder.and(study.studyState.eq(StudyState.RECRUITING));

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
            case COMPLETED:
                query.where(study.studyState.eq(StudyState.COMPLETED));
                query.orderBy(study.createdAt.desc());
                break;
            default:
                query.orderBy(study.createdAt.desc());
                break;
        }
        return query.fetch();
    }

    @Override
    public List<Study> findStudyByConditionsAndThemeTypesAndNotInIds(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<StudyTheme> themeTypes, List<Long> studyIds) {
        QStudy study = QStudy.study;

        BooleanBuilder builder = getBooleanBuilderByThemeTypes(search, study, themeTypes);
        if (sortBy != null) {
            switch (sortBy) {
                case RECRUITING:
                    builder.and(study.studyState.eq(StudyState.RECRUITING));
                    break;
                case COMPLETED:
                    builder.and(study.studyState.eq(StudyState.COMPLETED));
                    break;
                default:
                    break;
            }
        }

        // 정렬 조건 설정
        JPAQuery<Study> query = queryFactory.selectFrom(study)
            .where(builder)
            .where(study.id.notIn(studyIds))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        getSortBy(sortBy, query, study);

        return query.fetch();
    }


    @Override
    public List<Study> findStudyByConditionsAndRegionStudiesAndNotInIds(Map<String, Object> search,
        StudySortBy sortBy, Pageable pageable, List<RegionStudy> regionStudies,
        List<Long> studyIds) {
        QStudy study = QStudy.study;

        BooleanBuilder builder = getBooleanBuilderByRegionStudies(search, study, regionStudies);
        if (sortBy != null) {
            switch (sortBy) {
                case RECRUITING:
                    builder.and(study.studyState.eq(StudyState.RECRUITING));
                    break;
                case COMPLETED:
                    builder.and(study.studyState.eq(StudyState.COMPLETED));
                    break;
                default:
                    // 다른 조건이 필요하면 추가
                    break;
            }
        }

        // 정렬 조건 설정
        JPAQuery<Study> query = queryFactory.selectFrom(study)
            .where(builder)
            .where(study.id.notIn(studyIds))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        getSortBy(sortBy, query, study);

        return query.fetch();
    }

    @Override
    public List<Study> searchByTitle(String keyword, StudySortBy sortBy, Pageable pageable) {
        QStudy study = QStudy.study;

        // FULLTEXT SEARCH를 위한 서브쿼리 생성
        String subQuery = """
SELECT id FROM study WHERE MATCH(title) AGAINST (:keyword IN NATURAL LANGUAGE MODE) ORDER BY created_at DESC LIMIT :offset, :limit """;

        List<Long> ids = entityManager.createNativeQuery(subQuery)
                .setParameter("keyword", keyword)
                .setParameter("offset", pageable.getOffset())
                .setParameter("limit", pageable.getPageSize())
                .getResultList();

        // QueryDSL로 해당 ID들을 조회
        return queryFactory.selectFrom(study)
                .where(study.id.in(ids))
                .orderBy(study.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Study> findAllByTitleContaining(String title, StudySortBy sortBy,
        Pageable pageable) {
        QStudy study = QStudy.study;
        BooleanBuilder builder = new BooleanBuilder();
        getStudyState(sortBy, builder, study);

        // 정렬 조건 설정
        JPAQuery<Study> query = queryFactory.selectFrom(study)
            .where(builder)
            .where(study.title.contains(title))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        getSortBy(sortBy, query, study);
        return query.fetch();
    }

    @Override
    public List<Study> findByStudyTheme(List<StudyTheme> studyThemes, StudySortBy sortBy, Pageable pageable) {
        QStudy study = QStudy.study;
        BooleanBuilder builder = new BooleanBuilder();
        getStudyState(sortBy, builder, study);

        // 정렬 조건 설정
        JPAQuery<Study> query = queryFactory.selectFrom(study)
            .where(builder)
            .where(study.studyThemes.any().in(studyThemes))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        getSortBy(sortBy, query, study);
        return query.fetch();
    }

    @Override
    public List<Study> findByMemberStudy(List<MemberStudy> memberStudy, Pageable pageable) {
        QStudy study = QStudy.study;
        return queryFactory.selectFrom(study)
            .where(study.memberStudies.any().in(memberStudy))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Study> findRecruitingStudiesByMemberStudy(List<MemberStudy> memberStudy,
        Pageable pageable) {
        QStudy study = QStudy.study;
        return queryFactory.selectFrom(study)
            .where(study.memberStudies.any().in(memberStudy))
            .where(study.studyState.eq(StudyState.RECRUITING))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }



    @Override
    public long countStudyByConditionsAndThemeTypesAndNotInIds(Map<String, Object> search,
        List<StudyTheme> themeTypes, StudySortBy sortBy, List<Long> studyIds) {
        BooleanBuilder builder = getBooleanBuilderByThemeTypes(search, study, themeTypes);
        getStudyState(sortBy, builder, study);
        return queryFactory.selectFrom(study)
            .where(builder)
            .where(study.id.notIn(studyIds))
            .fetchCount();
    }


    @Override
    public long countStudyByConditionsAndRegionStudiesAndNotInIds(Map<String, Object> search,
        List<RegionStudy> regionStudies, StudySortBy sortBy, List<Long> studyIds) {
        BooleanBuilder builder = getBooleanBuilderByRegionStudies(search, study, regionStudies);
        getStudyState(sortBy, builder, study);
        return queryFactory.selectFrom(study)
            .where(builder)
            .where(study.id.notIn(studyIds))
            .fetchCount();
    }

    @Override
    public long countStudyByConditions(Map<String, Object> search, StudySortBy sortBy) {
        BooleanBuilder builder = new BooleanBuilder();
        getConditions(search, study, builder);
        getStudyState(sortBy, builder, study);
        return queryFactory.selectFrom(study)
            .where(builder)
            .fetchCount();
    }

    @Override
    public long countStudyByStudyTheme(List<StudyTheme> studyThemes, StudySortBy sortBy) {
        BooleanBuilder builder = new BooleanBuilder();
        getStudyState(sortBy, builder, study);
        return queryFactory.selectFrom(study)
            .where(builder)
            .where(study.studyThemes.any().in(studyThemes))
            .fetchCount();
    }

    @Override
    public long countAllByTitleContaining(String title, StudySortBy sortBy) {
        BooleanBuilder builder = new BooleanBuilder();
        getStudyState(sortBy, builder, study);
        return queryFactory.selectFrom(study)
            .where(builder)
            .where(study.title.contains(title))
            .fetchCount();
    }

    @Override
    public long countByMemberStudiesAndStatus(List<MemberStudy> memberStudies, Status status) {
        return queryFactory.selectFrom(study)
            .where(study.memberStudies.any().in(memberStudies))
            .where(study.status.eq(status))
            .fetchCount();
    }

    @Override
    public long countByMemberStudiesAndStatusAndIsOwned(List<MemberStudy> memberStudies, Status status,
                                                        boolean isOwned) {
        return queryFactory.selectFrom(study)
            .where(study.memberStudies.any().in(memberStudies))
            .where(study.status.eq(status))
            .where(study.memberStudies.any().isOwned.eq(isOwned))
            .fetchCount();
    }

    private static void getStudyState(StudySortBy sortBy, BooleanBuilder builder, QStudy study) {
        if (sortBy != null && sortBy.equals(StudySortBy.RECRUITING))
            builder.and(study.studyState.eq((StudyState.RECRUITING)));
        if (sortBy != null && sortBy.equals(StudySortBy.COMPLETED))
            builder.and(study.studyState.eq((StudyState.COMPLETED)));
    }


    private static void getSortBy(StudySortBy sortBy, JPAQuery<Study> query, QStudy study) {
        switch (sortBy) {
            case HIT:
                query.orderBy(study.hitNum.desc());
                query.orderBy(study.createdAt.desc());
                break;
            case LIKED:
                query.orderBy(study.heartCount.desc());
                query.orderBy(study.createdAt.desc());
                break;
            case COMPLETED:
                query.orderBy(study.createdAt.desc());
                break;
            case RECRUITING:
                query.orderBy(study.createdAt.desc());
                break;
            default:
                query.orderBy(study.createdAt.desc());
                break;
        }
    }
    private static BooleanBuilder getBooleanBuilderByRegionStudies(Map<String, Object> search, QStudy study,
        List<RegionStudy> RegionStudies) {
        BooleanBuilder builder = new BooleanBuilder();
        // 조건문 추가
        getConditions(search, study, builder);
        if (RegionStudies != null && !RegionStudies.isEmpty()) {
            builder.and(study.regionStudies.any().in(RegionStudies));
        }
        return builder;
    }

    private static BooleanBuilder getBooleanBuilderByThemeTypes(Map<String, Object> search, QStudy study,
        List<StudyTheme> themeTypes) {
        BooleanBuilder builder = new BooleanBuilder();

        // 조건문 추가
        getConditions(search, study, builder);
        if (themeTypes != null && !themeTypes.isEmpty()) {
            builder.and(study.studyThemes.any().in(themeTypes));
        }
        return builder;
    }
    private static void getConditions(Map<String, Object> search, QStudy study,
        BooleanBuilder builder) {
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
        if (search.get("themeTypes") != null) {
            List<ThemeType> themeTypes = (List<ThemeType>) search.get("themeTypes");
            builder.and(study.studyThemes.any().theme.studyTheme.in(themeTypes));
        }
    }

}
