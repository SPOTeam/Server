package com.example.spot.domain.mapping;

import com.example.spot.domain.Theme;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@DynamicUpdate
@DynamicInsert
@AllArgsConstructor
public class StudyTheme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    //== 테마 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    //== 스터디 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

/* ----------------------------- 생성자 ------------------------------------- */

    protected StudyTheme() {}

    @Builder
    public StudyTheme(Theme theme, Study study) {
        this.theme = theme;
        this.study = study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
