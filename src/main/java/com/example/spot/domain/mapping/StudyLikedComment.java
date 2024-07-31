package com.example.spot.domain.mapping;

import com.example.spot.domain.Member;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.study.StudyPostComment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyLikedComment extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 1")
    private Boolean isLiked;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_post_comment_id", nullable = false)
    private StudyPostComment studyPostComment;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public StudyLikedComment(StudyPostComment studyPostComment, Member member) {
        this.studyPostComment = studyPostComment;
        this.member = member;
    }

}
