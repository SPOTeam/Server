package com.example.spot.repository.querydsl.impl;

import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.study.QStudyPost;
import com.example.spot.domain.study.StudyPost;
import com.example.spot.repository.querydsl.StudyPostRepositoryCustom;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class StudyPostRepositoryCustomImpl implements StudyPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StudyPost> findAnnouncementsByStudyId(Long studyId, Pageable pageable) {
        QStudyPost studyPost = QStudyPost.studyPost;
        return queryFactory.selectFrom(studyPost)
                .where(studyPost.study.id.eq(studyId))
                .where(studyPost.isAnnouncement.eq(true))
                .orderBy(studyPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<StudyPost> findAllByStudyIdAndTheme(Long studyId, Theme theme, Pageable pageable) {

        QStudyPost studyPost = QStudyPost.studyPost;
        return queryFactory.selectFrom(studyPost)
                .where(studyPost.study.id.eq(studyId))  // studyId가 일치하는지 확인
                .where(studyPost.theme.eq(theme))       // theme이 일치하는지 확인
                .orderBy(studyPost.createdAt.desc())    // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<StudyPost> findAllByStudyId(Long studyId, Pageable pageable) {

        QStudyPost studyPost = QStudyPost.studyPost;
        return queryFactory.selectFrom(studyPost)
                .where(studyPost.study.id.eq(studyId))  // studyId가 일치하는지 확인
                .orderBy(studyPost.createdAt.desc())    // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
