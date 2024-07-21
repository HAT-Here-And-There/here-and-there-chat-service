package com.hat.hereandthere.chatservice.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Repository
public class ChatRepositoryCustomImpl implements ChatRepositoryCustom {
    static final String PLACE_ID = "placeId";
    static final String CHAT_COUNT = "chatCount";
    static final String REPLY_COUNT = "replyCount";
    static final String TOTAL_CHAT_COUNT = "totalChatCount";

    private final MongoTemplate mongoTemplate;

    public ChatRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Map<Long, Integer> countByPlaceIds(Collection<Long> placeIds) {

        final MatchOperation matchOperation = Aggregation.match(Criteria.where(PLACE_ID).in(placeIds));

        final GroupOperation groupOperation = Aggregation.group(PLACE_ID)
                .count().as(CHAT_COUNT)
                .sum(ArrayOperators.Size.lengthOfArray("replies"))
                .as(REPLY_COUNT);

        final ProjectionOperation projectionOperation = Aggregation.project()
                .andExpression("_id").as(PLACE_ID)
                .andExpression(CHAT_COUNT + " + " + REPLY_COUNT).as(TOTAL_CHAT_COUNT)
                .andExclude("_id");

        final Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation, projectionOperation);

        final AggregationResults<Object> results = mongoTemplate.aggregate(aggregation, "chat", Object.class);

        final List<Map<String, Long>> chatCountStatList = results.getMappedResults().stream()
                .map((Object item) -> {
                    final Map<?, ?> tmp = (Map<?, ?>) item;
                    return Map.of(
                            PLACE_ID, Long.parseLong(tmp.get(PLACE_ID).toString()),
                            TOTAL_CHAT_COUNT, Long.parseLong(tmp.get(TOTAL_CHAT_COUNT).toString())
                    );
                }).toList();

        return placeIds.stream()
                .collect(Collectors.toMap(
                        placeId -> placeId,
                        placeId -> {
                            final Map<String, Long> tmpMap = chatCountStatList.stream()
                                    .filter(stats -> stats.get(PLACE_ID).equals(placeId))
                                    .findFirst()
                                    .orElse(Map.of(TOTAL_CHAT_COUNT, 0L));

                            return tmpMap.get(TOTAL_CHAT_COUNT).intValue();
                        }));
    }
}

