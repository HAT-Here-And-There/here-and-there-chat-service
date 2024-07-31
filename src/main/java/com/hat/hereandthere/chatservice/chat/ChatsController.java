package com.hat.hereandthere.chatservice.chat;

import com.hat.hereandthere.chatservice.chat.dto.ChatResponseDto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/chats")
public class ChatsController {

    final private ChatService chatService;

    public ChatsController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("")
    public ResponseEntity<List<ChatResponseDto>> getChats(
            @RequestParam() Long placeId,
            @RequestParam(required = false) String lastChatId,
            @RequestParam(defaultValue = "30") int pageSize) {
        return ResponseEntity.ok(chatService.getChats(placeId, lastChatId, pageSize));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<Long, Integer>> getChatCount(
            @RequestParam() String placeIds
    ) {
        try {
            final List<Long> placeIdList = Arrays.stream(placeIds.split(",")).map(Long::parseLong).toList();

            return ResponseEntity.ok(chatService.getChatCount(placeIdList));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
