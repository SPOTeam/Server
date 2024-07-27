package com.example.spot.repository;

import com.example.spot.domain.Theme;
import com.example.spot.domain.enums.ThemeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Optional<Theme> findByStudyTheme(ThemeType themeType);
    boolean existsByStudyTheme(ThemeType themeType);

}
