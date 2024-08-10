package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    //== 신고 당한 회원 ==//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public MemberReport(String content, Member member) {
        this.content = content;
        this.status = ReportStatus.IN_PROGRESS;
        this.member = member;
    }

}
