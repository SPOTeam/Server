package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import com.example.spot.domain.enums.Board;
import com.example.spot.domain.mapping.MemberScrap;
import jakarta.persistence.*;
import lombok.*;

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

    private int hit;

    private int likeNum;

    private int commentNum;

    private int hitNum;

    @Enumerated(EnumType.STRING)
    private Board board;

    @OneToMany(mappedBy = "post")
    private List<PostImage> postImageList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<LikedPost> likedPostList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostReport> postReportList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostComment> postCommentList = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<MemberScrap> memberScrapList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memder_id")
    private Member member;

}
