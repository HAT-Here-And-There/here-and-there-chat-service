package com.hat.hereandthere.chatservice.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.OffsetDateTime;
import lombok.NonNull;

@JsonSerialize
public record ChatReceiveMessage(@NonNull String id,
                                 @NonNull Long userId,
                                 @NonNull Long placeId,
                                 @NonNull String content,
                                 String originChatId,
                                 @NonNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX") OffsetDateTime timeStamp) {

}
