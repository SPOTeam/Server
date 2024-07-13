package com.example.spot.domain.study;

import com.example.spot.domain.Member;
import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Theme;
import com.example.spot.domain.mapping.StudyLikedPost;
import com.example.spot.domain.mapping.StudyPostImage;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
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
public class StudyPost extends BaseEntity {

    @Id @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false)
    private boolean isAnnouncement;

    @Column(nullable = false)
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

}
