package com.example.domain.study;

import com.example.domain.common.BaseEntity;
import com.example.domain.enums.Theme;
import com.example.domain.mapping.StudyLikedPost;
import com.example.domain.mapping.StudyPostImage;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    //private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
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
