package com.hat.hereandthere.chatservice.chat;

import java.util.Collection;
import java.util.Map;

public interface ChatRepositoryCustom {
    Map<Long, Integer> countByPlaceIds(Collection<Long> placeIds);
}
