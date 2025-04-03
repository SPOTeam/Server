package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.mapping.MemberScrap;
import com.example.spot.web.dto.post.PostUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isAdmin;

    @Column
    private boolean isAnonymous;

    private String title;

    private String content;

    private int scrapNum;

    private int commentNum;

    private int hitNum;

    private String image;

    @Enumerated(EnumType.STRING)
    private Board board;

    @OneToMany(mappedBy = "post")
    @Builder.Default
    private List<PostImage> postImageList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @Builder.Default
    private List<LikedPost> likedPostList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PostReport> postReportList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PostComment> postCommentList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MemberScrap> memberScrapList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void edit(PostUpdateRequest request, List<String> images) {
        if (StringUtils.hasText(request.getTitle())) {
            this.title = request.getTitle();
        }

        if (StringUtils.hasText(request.getContent())) {
            this.content = request.getContent();
        }

        this.isAnonymous = request.isAnonymous();

        this.image = (images != null && !images.isEmpty() && StringUtils.hasText(images.get(0)))
                ? images.get(0)
                : null;

        if (request.getType() != null) {
            this.board = request.getType();
        }
    }


    public void viewHit() {
        this.hitNum++;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        super.setCreatedAt(createdAt);
    }

    public void plusCommentNum() {
        this.commentNum++;
    }

    public void addComment(PostComment comment) {
        this.postCommentList.add(comment);
    }

}
