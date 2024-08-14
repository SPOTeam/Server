package com.example.spot.service.post;

import com.example.spot.repository.LikedPostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.spot.security.utils.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikedPostCommentQueryServiceImpl implements LikedPostCommentQueryService {
    private final LikedPostCommentRepository likedPostCommentRepository;

    //댓글 좋아요 수
    @Override
    public long countByPostCommentIdAndIsLikedTrue(Long postCommentId) {
        return likedPostCommentRepository.countByPostCommentIdAndIsLikedTrue(postCommentId);
    }

    //현재 사용자의 댓글 좋아요 여부
    @Override
    public boolean existsByMemberIdAndPostCommentIdAndIsLikedTrue(Long postCommentId) {
        Long currentUserId = getCurrentUserId();
        return likedPostCommentRepository.existsByMemberIdAndPostCommentIdAndIsLikedTrue(currentUserId, postCommentId);
    }

}
