package com.example.spot.repository.querydsl.impl;

import com.example.spot.domain.Post;
import com.example.spot.domain.QLikedPost;
import com.example.spot.domain.QPost;
import com.example.spot.domain.QPostComment;
import com.example.spot.domain.enums.Board;
import com.example.spot.repository.querydsl.PostRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private final QPost post = QPost.post;

    private final QPostComment comment = QPostComment.postComment;
    private final QLikedPost like = QLikedPost.likedPost;

    //댓글순
    @Override
    public List<Post> findTopByOrderByCommentCountDesc() {
        return jpaQueryFactory
                .selectFrom(post)
                .leftJoin(post.postCommentList, comment)
                .groupBy(post)
                .orderBy(comment.count().desc(), post.id.desc())//댓글 수가 같을 경우 게시글 최신순(게시글 아이디 큰 순)
                .limit(5)
                .fetch();
    }

    //추천순(좋아요순)
    @Override
    public List<Post> findTopByOrderByLikeNumDesc() {
        return jpaQueryFactory
                .selectFrom(post)
                .leftJoin(post.likedPostList, like).fetchJoin()
                .groupBy(post)
                .orderBy(like.count().desc(), post.id.desc()) // 좋아요 수가 같을 경우 게시글 최신순(게시글 아이디 큰 순)
                .limit(5)
                .fetch();
    }

    //실시간순
    @Override
    public List<Post> findTopByRealTimeScore() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);

        return jpaQueryFactory
                .selectFrom(post)
                .leftJoin(post.postCommentList, comment)
                .leftJoin(post.likedPostList, like).fetchJoin()
                .where(post.createdAt.after(twoHoursAgo))
                .groupBy(post)
                .orderBy(
                        post.hitNum.add(like.count()).add(comment.count()).desc(),
                        post.hitNum.desc(),
                        post.likedPostList.size().desc(),
                        post.postCommentList.size().desc()
//                        like.count().desc(),
//                        comment.count().desc()
                )
                .limit(5)
                .fetch();
    }

    //게시글 타입별 최신 게시글
    @Override
    public List<Post> findRepresentativePosts() {
        return jpaQueryFactory
                .selectFrom(post)
                .where(post.board.ne(Board.ALL).and(post.board.ne(Board.SPOT_ANNOUNCEMENT)))
                .orderBy(post.createdAt.desc())
                .groupBy(post.board)
                .fetch();
    }

    //공지 게시글 최신 5개
    @Override
    public List<Post> findAnnouncementPosts() {
        return jpaQueryFactory
                .selectFrom(post)
                .where(post.board.eq(Board.SPOT_ANNOUNCEMENT))
                .orderBy(post.createdAt.desc())
                .limit(5)
                .fetch();
    }

}
