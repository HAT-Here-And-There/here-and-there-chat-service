package com.hat.hereandthere.chatservice.chat.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponseDto(String id, Long userId, String content, LocalDateTime timeStamp, List<ReplyResponseDto> replies) {

}
