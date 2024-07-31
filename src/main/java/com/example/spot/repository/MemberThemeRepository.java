package com.example.spot.repository;

import com.example.spot.domain.mapping.MemberTheme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 관심 분야
@Repository
public interface MemberThemeRepository extends JpaRepository<MemberTheme, Long> {
    List<MemberTheme> findAllByMemberId(Long memberId);
    MemberTheme findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
    boolean existsByMemberId(Long memberId);

}
