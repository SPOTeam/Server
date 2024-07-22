package com.example.spot.repository;

import com.example.spot.domain.mapping.MemberStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberStudyRepository extends JpaRepository<MemberStudy, Long> {

    List<MemberStudy> findByMember_Id(Long memberId);
}
