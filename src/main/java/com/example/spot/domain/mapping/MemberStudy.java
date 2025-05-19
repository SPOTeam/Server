package com.example.spot.domain.mapping;

import com.example.spot.domain.Member;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.ApplicationStatus;
import com.example.spot.domain.enums.Status;
import com.example.spot.domain.study.Study;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Builder
@AllArgsConstructor
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
    @Setter
    private Boolean isOwned;

    @Column(columnDefinition = "text")
    private String introduction;

    // 해당 유저로 호스트를 위임하는 이유
    @Column(columnDefinition = "text")
    @Setter
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status activeStatus;

    private LocalDateTime finishedAt;

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
    public void disable() {
        this.activeStatus = Status.OFF;
        this.finishedAt = LocalDateTime.now();
    }
}
