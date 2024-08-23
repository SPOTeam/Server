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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                .leftJoin(post.postCommentList, comment).fetchJoin()
                //.groupBy(post)
                .orderBy(post.postCommentList.size().desc())
                //.orderBy(comment.count().desc(), post.id.desc())//댓글 수가 같을 경우 게시글 최신순(게시글 아이디 큰 순)
                .limit(5)
                .fetch();
    }

    //추천순(좋아요순)
    @Override
    public List<Post> findTopByOrderByLikeNumDesc() {
        return jpaQueryFactory
                .selectFrom(post)
                .leftJoin(post.likedPostList, like).fetchJoin()
                //.groupBy(post)
                .orderBy(post.likedPostList.size().desc())
                //.orderBy(like.count().desc()) // 좋아요 수가 같을 경우 게시글 최신순(게시글 아이디 큰 순)
                .limit(5)
                .fetch();
    }

    //실시간순
    @Override
    public List<Post> findTopByRealTimeScore() {
        // TODO 실시간 두시간 전 게시글만 통계
        //LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);

        return jpaQueryFactory
                .selectFrom(post)
                .leftJoin(post.postCommentList, comment)
                .leftJoin(post.likedPostList, like).fetchJoin()
                //.where(post.createdAt.after(twoHoursAgo))
                //.groupBy(post)
                .orderBy(
                        post.hitNum.add(post.likedPostList.size()).add(post.postCommentList.size()).desc(),
                        post.hitNum.desc(),
                        post.likedPostList.size().desc(),
                        post.postCommentList.size().desc()
                )
                .limit(5)
                .fetch();
    }

    //게시글 타입별 최신 게시글
    @Override
    public List<Post> findRepresentativePosts() {
        // 모든 게시판의 최신 게시글을 불러옵니다.
        List<Post> posts = jpaQueryFactory
                .selectFrom(post)
                .where(post.board.ne(Board.ALL).and(post.board.ne(Board.SPOT_ANNOUNCEMENT)))
                .orderBy(post.board.asc(), post.createdAt.desc())
                .fetch();
        // 게시판 타입별로 최신 게시글 하나씩만 남기고 필터링합니다.
        Map<Board, Post> latestPostsByBoard = new LinkedHashMap<>();
        for (Post post : posts) {
            latestPostsByBoard.putIfAbsent(post.getBoard(), post);
        }
        // 필터링된 게시글 리스트를 반환합니다.
        return new ArrayList<>(latestPostsByBoard.values());
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
