package com.example.spot.repository;

import com.example.spot.domain.enums.Board;
import com.example.spot.domain.mapping.MemberScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberScrapRepository extends JpaRepository<MemberScrap, Long> {
    Optional<MemberScrap> findByMemberIdAndPostId(Long memberId, Long postId);

    long countByPostId(Long postId);

    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    @Query("SELECT ms FROM MemberScrap ms LEFT JOIN FETCH ms.post p WHERE ms.member.id = :memberId ORDER BY ms.createdAt DESC")
    Page<MemberScrap> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT ms FROM MemberScrap ms LEFT JOIN FETCH ms.post p WHERE ms.member.id = :memberId AND p.board = :board ORDER BY ms.createdAt DESC")
    Page<MemberScrap> findByMemberIdAndPost_Board(@Param("memberId") Long memberId, @Param("board") Board board, Pageable pageable);
}
