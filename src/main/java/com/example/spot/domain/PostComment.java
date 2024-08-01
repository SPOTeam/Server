package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private boolean isAnonymous;

    private String content;

    private int likeNum;

    private int disLikeNum;

    @OneToMany(mappedBy = "postComment")
    @Builder.Default
    private List<LikedPostComment> likedPostCommentsList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;


    //대댓글
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private PostComment parentComment; //부모 댓글

    @OneToMany(mappedBy = "parentComment", orphanRemoval = true)
    @Builder.Default
    private List<PostComment> childrenComment = new ArrayList<>(); //자식 댓글들(대댓글)
}
