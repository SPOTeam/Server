package com.example.spot.repository;

import com.example.spot.domain.study.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<Study, Long>{

    Page<Study> findAll(Specification<Study> spec, Pageable pageable);
}
