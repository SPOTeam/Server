package com.example.spot.domain.mapping;

import com.example.spot.domain.Member;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.enums.StudyLikeStatus;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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
public class PreferredStudy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    //== 회원 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //== 스터디 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyLikeStatus studyLikeStatus;

    // ------------------------------
    public void changeStatus(StudyLikeStatus status) {
        this.studyLikeStatus = status;
    }


}
