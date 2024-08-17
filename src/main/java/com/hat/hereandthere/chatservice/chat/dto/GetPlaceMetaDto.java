package com.hat.hereandthere.chatservice.chat.dto;

import lombok.Builder;

@Builder
public record GetPlaceMetaDto(
        Long placeId,
        Long majorRegionId,
        Long areaId,
        Long sigunguId
) {
}
