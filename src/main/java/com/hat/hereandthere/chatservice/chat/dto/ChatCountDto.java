package com.hat.hereandthere.chatservice.chat.dto;

import java.util.List;

public record ChatCountDto(
        List<Item> placeChatCountList
) {
    public record Item(
            Long placeId,
            Long chatCount
    ) {
    }
}
