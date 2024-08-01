package com.example.spot.scheduler;

import com.example.spot.domain.Post;
import com.example.spot.domain.PostScheduleLikes;
import com.example.spot.repository.PostRepository;
import com.example.spot.repository.PostScheduleLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSortLikesScheduler {

    private final PostRepository postRepository;
    private final PostScheduleLikesRepository postScheduleLikesRepository;

    @Transactional
    @Scheduled(cron = "0 0 13,18 * * ?", zone = "Asia/Seoul")
    public void generatePostSortLikes() {
        List<Post> topByOrderByLikePosts = postRepository.findTopByOrderByLikeNumDesc();
        List<PostScheduleLikes> postScheduleLikesList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Post post = topByOrderByLikePosts.get(i);
            PostScheduleLikes postScheduleLikes = PostScheduleLikes.of(post, i + 1);
            postScheduleLikesList.add(postScheduleLikes);
        }

        postScheduleLikesRepository.saveAll(postScheduleLikesList);
    }
}
