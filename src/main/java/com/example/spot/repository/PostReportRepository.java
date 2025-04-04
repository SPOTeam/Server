package com.example.spot.repository;

import com.example.spot.domain.PostReport;
import com.example.spot.domain.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    boolean existsByPostIdAndPostStatus(Long postId, PostStatus postStatus);
}
