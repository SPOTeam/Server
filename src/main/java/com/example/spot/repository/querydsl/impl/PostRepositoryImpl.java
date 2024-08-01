package com.example.spot.repository.querydsl.impl;

import com.example.spot.domain.Post;
import com.example.spot.domain.QPost;
import com.example.spot.domain.QPostComment;
import com.example.spot.repository.querydsl.PostRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private final QPost post = QPost.post;

    private final QPostComment comment = QPostComment.postComment;

    @Override
    public List<Post> findTopByOrderByCommentCountDesc() {
        return jpaQueryFactory
                .selectFrom(post)
                .leftJoin(comment)
                .groupBy(post)
                .orderBy(comment.count().desc(), post.id.desc())//댓글 수가 같을 경우 게시글 최신순(게시글 아이디 큰 순)
                .limit(5)
                .fetch();
    }
}
