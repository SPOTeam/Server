package com.example.spot.domain.mapping;

import com.example.spot.domain.Theme;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class StudyTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    //== 테마 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    //== 스터디 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;
}
