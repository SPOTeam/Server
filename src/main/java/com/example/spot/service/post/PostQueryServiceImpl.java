package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.Post;
import com.example.spot.domain.enums.Board;
import com.example.spot.web.dto.post.PostPagingDetailResponse;
import com.example.spot.web.dto.post.PostPagingResponse;
import com.example.spot.web.dto.post.PostSingleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.spot.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    @Transactional(readOnly = true)
    @Override
    public PostSingleResponse getPostById(Long postId) {
        // 게시글 단건 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 게시글이 신고되었는지 확인
        if (isPostReported(post)) {
            throw new PostHandler(ErrorStatus._POST_REPORTED);
        }

        // 조회된 게시글을 PostSingleResponse로 변환하여 반환
        return PostSingleResponse.toDTO(post);
    }

    @Transactional(readOnly = true)
    @Override
    public PostPagingResponse getPagingPosts(String type, Pageable pageable) {
        //게시글 페이징 조회
        Board boardType = Board.findByValue(type);
        if (boardType == null) {
            throw new PostHandler(ErrorStatus._INVALID_BOARD_TYPE);
        }

        // 신고되지 않은 게시글만 조회
        Page<Post> postPage = postRepository.findByBoardAndPostReportListIsEmpty(boardType, pageable);

        return PostPagingResponse.builder()
                .postType(type)
                .postResponses(postPage.getContent().stream()
                        .map(PostPagingDetailResponse::toDTO)
                        .collect(Collectors.toList()))
                .totalPage(postPage.getTotalPages())
                .totalElements(postPage.getTotalElements())
                .isFirst(postPage.isFirst())
                .isLast(postPage.isLast())
                .build();
    }

    // 게시글이 신고되었는지 확인하는 메서드
    private static boolean isPostReported(Post post) {
        return !post.getPostReportList().isEmpty();
    }
}
