package com.example.spot.domain;

import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberTheme;
import com.example.spot.domain.mapping.StudyTheme;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThemeType studyTheme;

    //== 해당 테마를 선호하는 멤버 목록 ==//
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<MemberTheme> memberThemeList;

    //== 테마별 스터디 목록 ==//
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<StudyTheme> studyThemeList;

}
