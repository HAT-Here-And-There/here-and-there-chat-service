package com.hat.hereandthere.chatservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hat.hereandthere.chatservice.chat.ChatService;
import com.hat.hereandthere.chatservice.chat.dto.ChatReceiveMessage;
import com.hat.hereandthere.chatservice.chat.dto.ChatSendMessage;
import com.hat.hereandthere.chatservice.utils.WebSocketSessionUtils;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class PlaceChatHandler extends TextWebSocketHandler {

  private final ChatService chatService;

//  private final RedisMessageSubscriber redisMessageSubscriber;
//  private final RedisTemplate<String, Object> redisTemplate;

  private final ObjectMapper objectMapper = new ObjectMapper();


  public PlaceChatHandler(
      ChatService chatService
//      RedisMessageSubscriber redisMessageSubscriber, RedisTemplate<String, Object> redisTemplate
  ) {
//    this.redisMessageSubscriber = redisMessageSubscriber;
//    this.redisTemplate = redisTemplate;
    this.chatService = chatService;
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
//    redisMessageSubscriber.addSession(session);
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
//    redisMessageSubscriber.removeSession(session);
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
    final Long placeId = WebSocketSessionUtils.getChannelId(session);
//    final String channel = "place:" + WebSocketSessionUtils.getChannelId(session);

    final ChatSendMessage chatSendMessage;
    try {
      chatSendMessage = objectMapper.readValue(message.getPayload(), ChatSendMessage.class);

      if (chatSendMessage.originChatId() == null) {
        chatService.saveChat(
            chatSendMessage.userId(),
            placeId,
            chatSendMessage.content()
        );
      } else {
        chatService.saveReply(
            chatSendMessage.userId(),
            chatSendMessage.content(),
            chatSendMessage.originChatId()
        );
      }

      final ChatReceiveMessage chatReceiveMessage = new ChatReceiveMessage(
          UUID.randomUUID().toString(),
          chatSendMessage.userId(),
          WebSocketSessionUtils.getChannelId(session),
          chatSendMessage.content(),
          chatSendMessage.originChatId(),
          OffsetDateTime.now(ZoneOffset.UTC)
      );

      session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatReceiveMessage)));

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
