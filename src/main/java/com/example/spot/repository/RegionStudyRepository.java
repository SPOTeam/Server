package com.example.spot.repository;

import com.example.spot.domain.Region;
import com.example.spot.domain.mapping.RegionStudy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionStudyRepository extends JpaRepository<RegionStudy, Long> {
    List<RegionStudy> findAllByRegion(Region region);

}
