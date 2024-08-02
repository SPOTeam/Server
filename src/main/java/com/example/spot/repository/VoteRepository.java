package com.example.spot.repository;

import com.example.spot.domain.study.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByIdAndStudyId(Long voteId, Long studyId);

    // 진행중인 투표 목록
    List<Vote> findAllByStudyIdAndFinishedAtAfter(Long studyId, LocalDateTime now);

    // 마감된 투표 목록
    List<Vote> findAllByStudyIdAndFinishedAtBefore(Long studyId, LocalDateTime now);
}
