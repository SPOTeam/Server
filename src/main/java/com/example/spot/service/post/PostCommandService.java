package com.example.spot.service.post;

import com.example.spot.web.dto.post.*;
import org.springframework.transaction.annotation.Transactional;


public interface PostCommandService {

    //게시글 생성
    PostCreateResponse createPost(Long memberId, PostCreateRequest postCreateRequest);

    //게시글 삭제
    void deletePost(Long memberId, Long postId);

    //게시글 수정
    PostCreateResponse updatePost(Long memberId, Long postId, PostUpdateRequest postUpdateRequest);

    //게시글 좋아요
    PostLikeResponse likePost(Long postId, Long memberId);

    //게시글 좋아요 취소
    PostLikeResponse cancelPostLike(Long postId, Long memberId);

    //게시글 댓글 생섣
    CommentCreateResponse createComment(Long postId, Long memberId, CommentCreateRequest request);

    //게시글 댓글 좋아요
    CommentLikeResponse likeComment(Long CommentId, Long memberId);

}
