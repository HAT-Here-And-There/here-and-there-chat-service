package com.hat.hereandthere.chatservice.chat.dto;

import lombok.NonNull;

public record ChatSendMessage(@NonNull Long userId, @NonNull String content, String originChatId) {

}
