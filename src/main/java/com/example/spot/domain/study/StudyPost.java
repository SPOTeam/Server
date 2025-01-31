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

@Entity
@Getter
@Builder
@AllArgsConstructor
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

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer likeNum = 0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer hitNum = 0;

    @Setter
    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer commentNum = 0;

    @Builder.Default
    @OneToMany(mappedBy = "studyPost", cascade = CascadeType.ALL)
    private List<StudyPostImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "studyPost", cascade = CascadeType.ALL)
    private List<StudyPostComment> comments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "studyPost", cascade = CascadeType.ALL)
    private List<StudyLikedPost> likedPosts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "studyPost", cascade = CascadeType.ALL)
    private List<StudyPostReport> studyPostReports = new ArrayList<>();

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
        member.updateStudyPost(this);
        study.updateStudyPost(this);
    }

    public void plusLikeNum() {
        likeNum++;
        member.updateStudyPost(this);
        study.updateStudyPost(this);
    }

    public void minusLikeNum() {
        likeNum--;
        member.updateStudyPost(this);
        study.updateStudyPost(this);
    }

    public void addStudyPostReport(StudyPostReport studyPostReport) {
        studyPostReports.add(studyPostReport);
    }
}
