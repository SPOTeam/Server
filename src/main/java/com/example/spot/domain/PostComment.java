package com.example.spot.domain;

import com.example.spot.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private int like_num;

    private int dislike_num;

    @OneToMany(mappedBy = "postComment")
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
    private List<PostComment> childrenComment = new ArrayList<>(); //자식 댓글들(대댓글)
}
