package com.example.spot.domain.mapping;

import com.example.spot.domain.Member;
import com.example.spot.domain.Quiz;
import com.example.spot.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
public class MemberAttendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isCorrect;

    //== 회원 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    //== 출석 ==//
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

/* ----------------------------- 생성자 ------------------------------------- */

    public MemberAttendance(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

}
