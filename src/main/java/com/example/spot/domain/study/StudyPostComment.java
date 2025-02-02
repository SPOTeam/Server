package com.example.spot.domain.study;

import com.example.spot.domain.Member;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.mapping.StudyLikedComment;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPostComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_post_id", nullable = false)
    private StudyPost studyPost;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer likeCount;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer dislikeCount;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isAnonymous;

    @Column
    private Integer anonymousNum;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private StudyPostComment parentComment;

    @Builder.Default
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<StudyPostComment> childrenComment = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "studyPostComment", cascade = CascadeType.ALL)
    private List<StudyLikedComment> likedComments = new ArrayList<>();

/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addChildrenComment(StudyPostComment studyPostComment) {
        childrenComment.add(studyPostComment);
        studyPostComment.setParentComment(this);
    }

    public void addLikedComment(StudyLikedComment studyLikedComment) {
        likedComments.add(studyLikedComment);
        studyLikedComment.setStudyPostComment(this);
    }

    public void deleteLikedComment(StudyLikedComment studyLikedComment) {
        likedComments.remove(studyLikedComment);
    }

    public void deleteComment() {
        content = "삭제된 댓글입니다.";
        isDeleted = Boolean.TRUE;
    }

    public void plusLikeCount() {
        likeCount++;
    }

    public void minusLikeCount() {
        likeCount--;
    }

    public void plusDislikeCount() {
        dislikeCount++;
    }

    public void minusDislikeCount() {
        dislikeCount--;
    }

}
