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
@DynamicUpdate
@DynamicInsert
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

    @Column(nullable = false)
    private Integer likeCount;

    @Column(nullable = false)
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

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<StudyPostComment> childrenComment;

    @OneToMany(mappedBy = "studyPostComment", cascade = CascadeType.ALL)
    private List<StudyLikedComment> likedComments;

/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public StudyPostComment(StudyPost studyPost, Member member, String content,
                            Boolean isAnonymous, Integer anonymousNum, StudyPostComment parentComment) {
        this.studyPost = studyPost;
        this.member = member;
        this.content = content;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.isAnonymous = isAnonymous;
        this.anonymousNum = anonymousNum;
        this.isDeleted = Boolean.FALSE;
        this.parentComment = parentComment;
        this.childrenComment = new ArrayList<>();
        this.likedComments = new ArrayList<>();
    }

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
