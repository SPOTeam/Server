package com.example.spot.repository;

import com.example.spot.domain.mapping.MemberScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberScrapRepository extends JpaRepository<MemberScrap, Long> {
    Optional<MemberScrap> findByMemberIdAndPostId(Long memberId, Long postId);

    long countByPostId(Long postId);

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);
}
