package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.Post;
import com.example.spot.domain.enums.Board;
import com.example.spot.web.dto.post.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.spot.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    private final LikedPostQueryService likedPostQueryService;

    @Transactional
    @Override
    public PostSingleResponse getPostById(Long postId) {
        // 게시글 단건 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 게시글이 신고되었는지 확인 - 수정 예정
//        if (isPostReported(post)) {
//            throw new PostHandler(ErrorStatus._POST_REPORTED);
//        }

        // 조회수 증가
        post.viewHit();

        // 좋아요 수 조회
        long likeCount = likedPostQueryService.countByPostId(postId);

        // 조회된 게시글을 PostSingleResponse로 변환하여 반환
        return PostSingleResponse.toDTO(post, likeCount);
    }

    @Transactional(readOnly = true)
    @Override
    public PostPagingResponse getPagingPosts(String type, Pageable pageable) {
        //게시글 페이징 조회
        Board boardType = Board.findByValue(type);
        if (boardType == null) {
            throw new PostHandler(ErrorStatus._INVALID_BOARD_TYPE);
        }

        // 신고되지 않은 게시글만 조회 -수정 예정
        Page<Post> postPage = postRepository.findByBoardAndPostReportListIsEmpty(boardType, pageable);

        List<PostPagingDetailResponse> postResponses = postPage.getContent().stream()
                .map(post -> {
                    long likeCount = likedPostQueryService.countByPostId(post.getId());
                    return PostPagingDetailResponse.toDTO(post, likeCount);
                })
                .toList();

        return PostPagingResponse.builder()
                .postType(type)
                .postResponses(postResponses)
                .totalPage(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .isFirst(postPage.isFirst())
                .isLast(postPage.isLast())
                .build();
    }

    // 게시글이 신고되었는지 확인하는 메서드
    private boolean isPostReported(Post post) {
        return !post.getPostReportList().isEmpty();
    }

    @Transactional(readOnly = true)
    @Override
    public PostBest5Response getPostBest(String sortType) {

        if (sortType.equals("REAL_TIME")) {
            // 실시간 조회 후 리턴

        }

        if (sortType.equals("RECOMMEND")) {
            // 추천수(좋아요수) 조회 후 리턴
            List<Post> posts = postRepository.findTopByOrderByLikeNumDesc();
            List<PostBest5DetailResponse> responses = posts.stream()
                    .map(post -> PostBest5DetailResponse.from(post, posts.indexOf(post) + 1))
                    .toList();

            return PostBest5Response.builder()
                    .sortType("RECOMMEND")
                    .postBest5Responses(responses)
                    .build();
        }

        if (sortType.equals("COMMENT")) {
            // 댓글 수 조회 후 리턴
            // 검색 후 DTO 리턴
            List<Post> posts = postRepository.findTopByOrderByCommentCountDesc();
            List<PostBest5DetailResponse> responses = posts.stream()
                    .map(post -> PostBest5DetailResponse.from(post, posts.indexOf(post) + 1))
                    .toList();

            return PostBest5Response.builder()
                    .sortType("COMMENT")
                    .postBest5Responses(responses)
                    .build();

        }

        // 무조건 에러
        throw new PostHandler(ErrorStatus._INVALID_SORT_TYPE);
    }
}
