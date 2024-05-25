package com.hat.hereandthere.chatservice.chat.dto;

import java.time.LocalDateTime;

public record ReplyResponseDto(String id, Long userId, String content, LocalDateTime timeStamp) {

}
