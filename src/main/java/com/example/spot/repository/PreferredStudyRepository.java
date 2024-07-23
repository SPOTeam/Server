package com.example.spot.repository;

import com.example.spot.domain.mapping.PreferredStudy;
import org.springframework.data.jpa.repository.JpaRepository;

// 찜한 스터디
public interface PreferredStudyRepository extends JpaRepository<PreferredStudy, Long> {

}
