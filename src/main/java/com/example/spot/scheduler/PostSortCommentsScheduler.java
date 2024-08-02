package com.example.spot.scheduler;

import com.example.spot.domain.Post;
import com.example.spot.domain.PostScheduleComments;
import com.example.spot.repository.PostRepository;
import com.example.spot.repository.PostScheduleCommentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSortCommentsScheduler {

    private final PostRepository postRepository;
    private final PostScheduleCommentsRepository postScheduleRepository;

    @Transactional
    @Scheduled(cron = "0 0 13,18 * * ?", zone = "Asia/Seoul")
    public void generatePostSortComments() {
        List<Post> topByOrderByCommentPosts = postRepository.findTopByOrderByCommentCountDesc();
        List<PostScheduleComments> postScheduleCommentList = new ArrayList<>();

        int size = Math.min(topByOrderByCommentPosts.size(), 5); // 리스트 크기와 5 중 작은 값을 선택

        for (int i = 0; i < size; i++) {
            Post post = topByOrderByCommentPosts.get(i);
            PostScheduleComments postScheduleComments = PostScheduleComments.of(post, i + 1);
            postScheduleCommentList.add(postScheduleComments);
        }

        postScheduleRepository.saveAll(postScheduleCommentList);
    }
}
