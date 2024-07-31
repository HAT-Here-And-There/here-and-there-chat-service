package com.hat.hereandthere.chatservice.chat;

import com.hat.hereandthere.chatservice.chat.dto.GetPlaceMetaDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class ChatCountService {
    final private RedisTemplate<String, Long> redisTemplate;
    final private RestTemplate restTemplate;
    final private Environment env;

    public ChatCountService(
            RedisTemplate<String, Long> redisTemplate,
            RestTemplate restTemplate,
            Environment env
    ) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
        this.env = env;
    }

    private GetPlaceMetaDto getPlaceMetadataFromRedis(Long placeId) {
        final String key = String.format("place_metadata:%d", placeId);
        final boolean isCached = redisTemplate.opsForHash().hasKey(key, "major_region_id")
                && redisTemplate.opsForHash().hasKey(key, "sigungu_id");

        log.error("isCached: {}", isCached);
        if (isCached) {
            return new GetPlaceMetaDto(
                    placeId,
                    (Long) redisTemplate.opsForHash().get(key, "major_region_id"),
                    (Long) redisTemplate.opsForHash().get(key, "sigungu_id")
            );
        }

        final GetPlaceMetaDto newPlaceMetadata = getPlaceMetadataFromExternal(placeId);
        cachePlaceMetadata(placeId, newPlaceMetadata.majorRegionId(), newPlaceMetadata.sigunguId());

        return newPlaceMetadata;
    }

    private GetPlaceMetaDto getPlaceMetadataFromExternal(Long placeId) {
        String placeMetadataUrl = String.format("%s/places/meta/%d", env.getProperty("tour-service.url"), placeId);

        return restTemplate.getForObject(placeMetadataUrl, GetPlaceMetaDto.class);
    }

    private void cachePlaceMetadata(Long placeId, Long majorRegionId, Long sigunguId) {
        final String key = String.format("place_metadata:%d", placeId);
        redisTemplate.opsForHash().put(key, "major_region_id", majorRegionId);
        redisTemplate.opsForHash().put(key, "sigungu_id", sigunguId);
    }


    // add chat count
    @Transactional
    public void increaseChatCount(Long placeId) {
        log.error("increaseChatCount: {}", placeId);
        final GetPlaceMetaDto getPlaceMetaDto = getPlaceMetadataFromRedis(placeId);

        redisTemplate.opsForZSet().incrementScore("place_chat_count", placeId, 1);
        redisTemplate.opsForZSet()
                .incrementScore(String.format("major_region:%d:chat_count", getPlaceMetaDto.majorRegionId()), placeId, 1);
        redisTemplate.opsForZSet()
                .incrementScore(String.format("sigungu:%d:chat_count", getPlaceMetaDto.sigunguId()), placeId, 1);
    }

}
