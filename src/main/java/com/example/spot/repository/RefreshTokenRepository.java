package com.example.spot.repository;

import com.example.spot.domain.auth.RefreshToken;
import com.example.spot.domain.study.Option;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
    Optional<RefreshToken> findByToken(String token);
    void deleteByMemberId(Long memberId);
    void deleteAllByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    void deleteAllByMemberIdIn(List<Long> deletedMemberIds);
}
