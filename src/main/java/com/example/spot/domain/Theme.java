package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.ThemeType;
import com.example.spot.domain.mapping.MemberTheme;
import com.example.spot.domain.mapping.StudyTheme;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Theme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThemeType studyTheme;

    //== 해당 테마를 선호하는 멤버 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<MemberTheme> memberThemeList = new ArrayList<>();

    //== 테마별 스터디 목록 ==//
    @Builder.Default
    @OneToMany(mappedBy = "theme", cascade = CascadeType.ALL)
    private List<StudyTheme> studyThemeList = new ArrayList<>();

/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addMemberTheme(MemberTheme memberTheme) {
        memberThemeList.add(memberTheme);
        memberTheme.setTheme(this);
    }

    public void addStudyTheme(StudyTheme studyTheme) {
        studyThemeList.add(studyTheme);
        studyTheme.setTheme(this);
    }
}
