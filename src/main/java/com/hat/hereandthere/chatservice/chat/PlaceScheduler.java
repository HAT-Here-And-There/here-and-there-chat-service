package com.hat.hereandthere.chatservice.chat;

import com.hat.hereandthere.chatservice.chat.dto.GetPlaceMetaDto;
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
public class PlaceScheduler {
    private final ChatRepository chatRepository;
    private final ChatRepositoryCustom chatRepositoryCustom;
    private final RedisTemplate<String, Long> longRedisTemplate;
    private final PlaceService placeService;

    @Scheduled(cron = "0 */10 * * * ?")
    public void countChatsAndSaveToRedis() {
        log.info("counting chat task is started!");
        List<Long> placeIds = chatRepository.findDistinctPlaceIds();

        sync(chatRepositoryCustom.countByPlaceIds(placeIds));
        log.info("counting chat task is ended!");
    }

    private void sync(Map<Long, Integer> chatCounts) {
        for (Map.Entry<Long, Integer> entry : chatCounts.entrySet()) {
            Long placeId = entry.getKey();
            Integer chatCount = entry.getValue();

            try {
                final GetPlaceMetaDto placeMetaDto = placeService.getPlaceMetadataFromRedis(placeId);

                longRedisTemplate.opsForZSet().add(PlaceService.TOTAL_RANKING_KEY, placeId, chatCount);
                longRedisTemplate.opsForZSet().add(
                        String.format(PlaceService.MAJOR_RANKING_KEY_FORMAT, placeMetaDto.majorRegionId()),
                        placeId,
                        chatCount
                );
                longRedisTemplate.opsForZSet().add(
                        String.format(PlaceService.SIGUNGU_RANKING_KEY_FORMAT, placeMetaDto.areaId(), placeMetaDto.sigunguId()),
                        placeId,
                        chatCount
                );
            } catch (Exception e) {
                log.error("Synchronize chat count scheduler throw excpetion: placeId-{} \n" +
                        "\t\t Exception message is => {}", placeId, e.getMessage());
            }
        }
    }
}
