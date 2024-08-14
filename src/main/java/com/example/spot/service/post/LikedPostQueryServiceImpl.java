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

    @Override
    public long countByPostId(Long postId) {
        return likedPostRepository.countByPostId(postId);
    }

    @Override
    public boolean existsByMemberIdAndPostId(Long postId) {
        Long currentUserId = getCurrentUserId();
        return likedPostRepository.existsByMemberIdAndPostId(currentUserId, postId);
    }
}
