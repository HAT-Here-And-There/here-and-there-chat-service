package com.hat.hereandthere.chatservice.chat;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableScheduling
@AllArgsConstructor
public class ChatCountScheduler {
    private final ChatRepository chatRepository;
    private final ChatRepositoryCustom chatRepositoryCustom;
    private final RedisTemplate<String, Long> longRedisTemplate;

    @Scheduled(cron = "0 0 0 * * ?")
    public void countChatsAndSaveToRedis() {
        log.info("counting chat task is started!");
        List<Long> placeIds = chatRepository.findDistinctPlaceIds();

        Map<Long, Integer> chatCounts = chatRepositoryCustom.countByPlaceIds(placeIds);

        chatCounts.forEach((placeId, chatCount) -> longRedisTemplate.opsForZSet().add("place_chat_count", placeId, chatCount));
        log.info("counting chat task is ended!");
    }
}
