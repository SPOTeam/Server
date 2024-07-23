package com.example.spot.repository;

import com.example.spot.domain.mapping.MemberTheme;
import org.springframework.data.jpa.repository.JpaRepository;

// 관심 분야
public interface MemberThemeRepository extends JpaRepository<MemberTheme, Long> {

}
