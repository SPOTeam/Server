package com.example.spot.service.post;

import com.example.spot.repository.LikedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.spot.security.utils.SecurityUtils.getCurrentUserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikedPostQueryServiceImpl implements LikedPostQueryService {

    private final LikedPostRepository likedPostRepository;

    /**
     * 게시글의 좋아요 수를 반환합니다.
     * @param postId 게시글 ID
     * @return 게시글의 좋아요 수
     */
    @Override
    public long countByPostId(Long postId) {
        return likedPostRepository.countByPostId(postId);
    }

    /**
     * 현재 사용자의 게시글 좋아요 여부를 true/false로 반환합니다.
     * @param postId 게시글 ID
     * @return 현재 사용자의 게시글 좋아요 여부
     */
    @Override
    public boolean existsByMemberIdAndPostId(Long postId) {
        Long currentUserId = getCurrentUserId();
        return likedPostRepository.existsByMemberIdAndPostId(currentUserId, postId);
    }
}
