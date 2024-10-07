package com.example.spot.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotKeywordScheduler {

    @Value("${study.keyword}")
    private String KEYWORD;
    @Value("${study.hot-keyword}")
    private String HOT_KEYWORD;
    @Value("${study.last-updated}")
    private String LAST_UPDATED;

    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    public HotKeywordScheduler(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 13시와 18시에 인기 검색어 목록을 업데이트 합니다.
    @Scheduled(cron = "0 0 13,18 * * *")
    public void updateHotKeywords() {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        // 인기 검색어 5개 가져오기
        Set<TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(KEYWORD, 0, 4);

        // HOT_KEYWORD 키에 갱신된 인기 검색어 저장
        if (typedTuples != null && !typedTuples.isEmpty()) {
            redisTemplate.delete(HOT_KEYWORD);

            for (TypedTuple<String> tuple : typedTuples)
                zSetOperations.add(HOT_KEYWORD, tuple.getValue(), tuple.getScore());

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            // 인기 검색어 업데이트 시점 저장
            redisTemplate.opsForValue().set(LAST_UPDATED, now);
            log.info("Hot keywords updated at {}", now);
        }
    }
}
