package com.example.spot.repository;

import com.example.spot.domain.mapping.MemberVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberVoteRepository extends JpaRepository<MemberVote, Long> {

    boolean existsByMemberIdAndOptionId(Long memberId, Long optionId);

    boolean existsByOptionId(Long optionId);

    List<MemberVote> findAllByOptionId(Long id);
}
