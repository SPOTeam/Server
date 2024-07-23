package com.example.spot.repository;

import com.example.spot.domain.mapping.RegionStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionStudyRepository extends JpaRepository<RegionStudy, Long> {
}
