package com.example.spot.repository;

import com.example.spot.domain.mapping.MemberAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberAttendanceRepository extends JpaRepository<MemberAttendance, Long> {
}
