package com.example.spot.repository;

import com.example.spot.domain.Theme;
import com.example.spot.domain.mapping.StudyTheme;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface StudyThemeRepository extends JpaRepository<StudyTheme, Long> {
    List<StudyTheme> findAllByTheme(Theme theme);

}