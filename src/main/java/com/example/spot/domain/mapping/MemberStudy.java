package com.example.spot.domain.mapping;

import com.example.spot.domain.Member;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class MemberStudy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Setter
    private ApplicationStatus status;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isOwned;

    @Column(columnDefinition = "text")
    private String introduction;

    //== 회원 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //== 스터디 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

/* ----------------------------- 생성자 ------------------------------------- */

    protected MemberStudy() {}

    @Builder
    public MemberStudy(Boolean isOwned, String introduction, Member member, Study study, ApplicationStatus status) {

        this.isOwned = isOwned;
        this.introduction = introduction;
        this.member = member;
        this.study = study;
        this.status = status;
    }


}
