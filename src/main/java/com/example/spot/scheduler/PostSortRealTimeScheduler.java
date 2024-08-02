package com.example.spot.scheduler;

import com.example.spot.domain.Post;
import com.example.spot.domain.PostScheduleRealTime;
import com.example.spot.repository.PostRepository;
import com.example.spot.repository.PostScheduleRealTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostSortRealTimeScheduler {
    private final PostRepository postRepository;
    private final PostScheduleRealTimeRepository postScheduleRealTimeRepository;

    @Transactional
    @Scheduled(cron = "0 0 13,18 * * ?", zone = "Asia/Seoul")
    public void generatePostSortRealTime() {
        List<Post> topByOrderByRealTimePosts = postRepository.findTopByRealTimeScore();
        List<PostScheduleRealTime> postScheduleRealTimeList = new ArrayList<>();

        int size = Math.min(topByOrderByRealTimePosts.size(), 5); // 리스트 크기와 5 중 작은 값을 선택

        for (int i = 0; i < size; i++) {
            Post post = topByOrderByRealTimePosts.get(i);
            PostScheduleRealTime postScheduleRealTime = PostScheduleRealTime.of(post, i + 1);
            postScheduleRealTimeList.add(postScheduleRealTime);
        }

        postScheduleRealTimeRepository.saveAll(postScheduleRealTimeList);
    }
}
