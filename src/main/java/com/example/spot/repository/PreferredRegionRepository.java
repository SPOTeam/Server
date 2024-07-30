package com.example.spot.repository;

import com.example.spot.domain.mapping.PreferredRegion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferredRegionRepository extends JpaRepository<PreferredRegion, Long> {

    List<PreferredRegion> findAllByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
