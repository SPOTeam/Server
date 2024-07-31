package com.example.spot.repository.querydsl;

import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.study.StudyPost;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyPostRepositoryCustom {

    // 테마별 스터디 게시글 페이징 조회
    List<StudyPost> findAllByStudyIdAndTheme(Long studyId, Theme theme, Pageable pageable);

    // 전체 스터디 게시글 페이징 조회
    List<StudyPost> findAllByStudyId(Long studyId, Pageable pageable);
}
