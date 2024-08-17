package com.hat.hereandthere.chatservice.chat;

import com.hat.hereandthere.chatservice.chat.dto.ChatCountDto;
import com.hat.hereandthere.chatservice.chat.dto.GetPlaceMetaDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Set;


@Slf4j
@Service
public class PlaceService {

    static final String TOTAL_RANKING_KEY = "place_chat_count";
    static final String SIGUNGU_RANKING_KEY_FORMAT = "area:%d:sigungu:%d:chat_count";
    static final String MAJOR_RANKING_KEY_FORMAT = "major_region:%d:chat_count";

    final private RedisTemplate<String, Long> redisTemplate;
    final private RestTemplate restTemplate;
    final private Environment env;

    public PlaceService(
            RedisTemplate<String, Long> redisTemplate,
            RestTemplate restTemplate,
            Environment env
    ) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
        this.env = env;
    }

    // add chat count
    @Transactional
    public void increaseChatCount(Long placeId) {
        try {
            final GetPlaceMetaDto getPlaceMetaDto = getPlaceMetadataFromRedis(placeId);

            redisTemplate.opsForZSet().incrementScore(TOTAL_RANKING_KEY, placeId, 1);
            redisTemplate.opsForZSet()
                    .incrementScore(String.format(MAJOR_RANKING_KEY_FORMAT, getPlaceMetaDto.majorRegionId()), placeId, 1);
            redisTemplate.opsForZSet()
                    .incrementScore(
                            String.format(SIGUNGU_RANKING_KEY_FORMAT, getPlaceMetaDto.areaId(), getPlaceMetaDto.sigunguId()),
                            placeId,
                            1
                    );
        } catch (RestClientException e) {
            log.error("increase chat count failed: {}", e.getMessage());
        }
    }

    public ChatCountDto getPlaceChatCount(int page, int pageSize) {
        final int start = (page - 1) * pageSize;
        final int end = page * pageSize - 1;

        final Set<ZSetOperations.TypedTuple<Long>> placeIdAndScore =
                redisTemplate.opsForZSet().reverseRangeWithScores(TOTAL_RANKING_KEY, start, end);

        if (placeIdAndScore == null) {
            return new ChatCountDto(List.of());
        }

        return new ChatCountDto(
                placeIdAndScore.stream()
                        .map(o ->
                                new ChatCountDto.Item(
                                        o.getValue(),
                                        Math.round(Objects.requireNonNullElse(o.getScore(), (double) 0))
                                )
                        ).toList()
        );
    }

    public ChatCountDto getPlaceChatCountByMajorRegionId(int page, int pageSize, Long majorRegionId) {
        final int start = (page - 1) * pageSize;
        final int end = page * pageSize - 1;

        final Set<ZSetOperations.TypedTuple<Long>> placeIdAndScore =
                redisTemplate.opsForZSet().reverseRangeWithScores(String.format(MAJOR_RANKING_KEY_FORMAT, majorRegionId), start, end);


        if (placeIdAndScore == null) {
            return new ChatCountDto(List.of());
        }

        return new ChatCountDto(
                placeIdAndScore.stream()
                        .map(o ->
                                new ChatCountDto.Item(
                                        o.getValue(),
                                        Math.round(Objects.requireNonNullElse(o.getScore(), (double) 0))
                                )
                        ).toList()
        );
    }

    public ChatCountDto getPlaceChatCountBySigunguId(int page, int pageSize, Long areaId, Long sigunguId) {
        final int start = (page - 1) * pageSize;
        final int end = page * pageSize - 1;

        final Set<ZSetOperations.TypedTuple<Long>> placeIdAndScore =
                redisTemplate.opsForZSet().reverseRangeWithScores(String.format(SIGUNGU_RANKING_KEY_FORMAT, areaId, sigunguId), start, end);


        if (placeIdAndScore == null) {
            return new ChatCountDto(List.of());
        }

        return new ChatCountDto(
                placeIdAndScore.stream()
                        .map(o ->
                                new ChatCountDto.Item(
                                        o.getValue(),
                                        Math.round(Objects.requireNonNullElse(o.getScore(), (double) 0))
                                )
                        ).toList()
        );
    }

    public GetPlaceMetaDto getPlaceMetadataFromRedis(Long placeId) {
        final String key = String.format("place_metadata:%d", placeId);
        final boolean isCached = redisTemplate.opsForHash().hasKey(key, "major_region_id")
                && redisTemplate.opsForHash().hasKey(key, "area_id")
                && redisTemplate.opsForHash().hasKey(key, "sigungu_id");

        if (isCached) {
            return new GetPlaceMetaDto(
                    placeId,
                    (Long) redisTemplate.opsForHash().get(key, "major_region_id"),
                    (Long) redisTemplate.opsForHash().get(key, "area_id"),
                    (Long) redisTemplate.opsForHash().get(key, "sigungu_id")
            );
        }

        final GetPlaceMetaDto newPlaceMetadata = getPlaceMetadataFromExternal(placeId);
        cachePlaceMetadata(
                placeId,
                newPlaceMetadata.majorRegionId(),
                newPlaceMetadata.areaId(),
                newPlaceMetadata.sigunguId()
        );

        return newPlaceMetadata;
    }

    private GetPlaceMetaDto getPlaceMetadataFromExternal(Long placeId) {
        try {
            String placeMetadataUrl = String.format("%s/places/meta/%d", env.getProperty("tour-service.url"), placeId);

            return restTemplate.getForObject(placeMetadataUrl, GetPlaceMetaDto.class);
        } catch (Exception e) {
            throw new RestClientException("Failed to get place metadata from tour service");
        }

    }

    private void cachePlaceMetadata(Long placeId, Long majorRegionId, Long areaId, Long sigunguId) {
        final String key = String.format("place_metadata:%d", placeId);
        redisTemplate.opsForHash().put(key, "major_region_id", majorRegionId);
        redisTemplate.opsForHash().put(key, "area_id", areaId);
        redisTemplate.opsForHash().put(key, "sigungu_id", sigunguId);
    }

}
