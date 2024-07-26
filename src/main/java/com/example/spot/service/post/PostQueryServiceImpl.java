package com.example.spot.service.post;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.PostHandler;
import com.example.spot.domain.Post;
import com.example.spot.web.dto.post.PostSingleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.spot.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;
    @Transactional(readOnly = true)
    @Override
    public PostSingleResponse getPostById(Long postId) {
        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus._POST_NOT_FOUND));

        // 게시글이 신고되었는지 확인
        if (isPostReported(post)) {
            throw new PostHandler(ErrorStatus._POST_REPORTED);
        }

        // 조회된 게시글을 PostSingleResponse로 변환하여 반환
        return PostSingleResponse.toDTO(post);
    }

    private static boolean isPostReported(Post post) {
        return !post.getPostReportList().isEmpty();
    }
}
