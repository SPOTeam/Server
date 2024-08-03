package com.example.spot.repository;

import com.example.spot.domain.study.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {

    Optional<Option> findByIdAndVoteId(Long optionId, Long voteId);

    List<Option> findAllByVoteId(Long voteId);
}
