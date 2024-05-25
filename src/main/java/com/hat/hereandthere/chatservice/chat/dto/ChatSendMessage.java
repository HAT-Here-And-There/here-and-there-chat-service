package com.hat.hereandthere.chatservice.chat.dto;

import lombok.NonNull;

public record ChatSendMessage(@NonNull String userId, @NonNull String content, String originChatId) {

}
