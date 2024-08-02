package com.example.spot.repository;

import com.example.spot.domain.mapping.MemberVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {

    boolean existsByMemberIdAndOptionId(Long memberId, Long optionId);
}
