package com.example.spot.repository;

import com.example.spot.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByProvince(String province);
    List<Region> findAllByDistrict(String district);
    Optional<Region> findByProvinceAndDistrictAndNeighborhood(String province, String district, String neighborhood);
    Region findByCode(String code);
}
