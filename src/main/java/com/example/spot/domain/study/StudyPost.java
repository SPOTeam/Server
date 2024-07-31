package com.example.spot.domain.study;

import com.example.spot.domain.Member;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.mapping.StudyLikedPost;
import com.example.spot.domain.mapping.StudyPostImage;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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
public class StudyPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isAnnouncement;

    @Setter
    @Column
    private LocalDateTime announcedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Theme theme;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Integer likeNum;

    @Column(nullable = false)
    private Integer hitNum;

    @Column(nullable = false)
    private Integer commentNum;

    @OneToMany(mappedBy = "studyPost", cascade = CascadeType.ALL)
    private List<StudyPostImage> images;

    @OneToMany(mappedBy = "studyPost", cascade = CascadeType.ALL)
    private List<StudyPostComment> comments;

    @OneToMany(mappedBy = "studyPost", cascade = CascadeType.ALL)
    private List<StudyLikedPost> likedPosts;

/* ----------------------------- 생성자 ------------------------------------- */

    @Builder
    public StudyPost(Boolean isAnnouncement, Theme theme, String title, String content) {
        this.isAnnouncement = isAnnouncement;
        this.theme = theme;
        this.title = title;
        this.content = content;
        this.likeNum = 0;
        this.hitNum = 0;
        this.commentNum = 0;
        this.images = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.likedPosts = new ArrayList<>();
    }
/* ----------------------------- 연관관계 메소드 ------------------------------------- */

    public void addImage(StudyPostImage image) {
        images.add(image);
        image.setStudyPost(this);
    }

    public void addComment(StudyPostComment comment) {
        comments.add(comment);
        comment.setStudyPost(this);
    }

    public void addLikedPost(StudyLikedPost likedPost) {
        likedPosts.add(likedPost);
        likedPost.setStudyPost(this);
    }

    public void deleteImage(StudyPostImage image) {
        images.remove(image);
    }

    public void deleteComment(StudyPostComment comment) {
        comments.remove(comment);
    }

    public void deleteLikedPost(StudyLikedPost likedPost) {
        likedPosts.remove(likedPost);
    }

    public void updateImage(StudyPostImage studyPostImage) {
        images.set(images.indexOf(studyPostImage), studyPostImage);
    }

    public void updateComment(StudyPostComment studyPostComment) {
        comments.set(comments.indexOf(studyPostComment), studyPostComment);
    }

    public void plusHitNum() {
        hitNum++;
    }

    public void plusLikeNum() {
        likeNum++;
    }

    public void minusLikeNum() {
        likeNum--;
    }
}
