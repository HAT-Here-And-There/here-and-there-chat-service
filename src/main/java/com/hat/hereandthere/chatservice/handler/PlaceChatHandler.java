package com.hat.hereandthere.chatservice.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hat.hereandthere.chatservice.chat.ChatService;
import com.hat.hereandthere.chatservice.chat.dto.ChatReceiveMessage;
import com.hat.hereandthere.chatservice.chat.dto.ChatSendMessage;
import com.hat.hereandthere.chatservice.chat.entity.Chat;
import com.hat.hereandthere.chatservice.chat.entity.Reply;
import com.hat.hereandthere.chatservice.utils.WebSocketSessionUtils;
import java.io.IOException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class PlaceChatHandler extends TextWebSocketHandler {

  private final RedisPublisher redisPublisher;
  private final RedisSubscriber redisSubscriber;
  private final ChatService chatService;
  private final ObjectMapper objectMapper;

  public PlaceChatHandler(
      RedisPublisher redisPublisher,
      RedisSubscriber redisSubscriber,
      ChatService chatService,
      ObjectMapper objectMapper) {
    this.redisPublisher = redisPublisher;
    this.redisSubscriber = redisSubscriber;
    this.chatService = chatService;
    this.objectMapper = objectMapper;
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    redisSubscriber.addSession(session);
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
    redisSubscriber.removeSession(session);
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws IOException {
    final Long placeId = WebSocketSessionUtils.getChannelId(session);
    final String channel = "place:" + placeId;

    try {
      final ChatSendMessage chatSendMessage = parseMessage(message);

      final ChatReceiveMessage receiveMessage = saveAndConvert(placeId, chatSendMessage);

      redisPublisher.publish(channel, objectMapper.writeValueAsString(receiveMessage));
    } catch (JsonProcessingException e) {
      log.error("Exception on parsing. {}", e.getMessage());
      session.close(CloseStatus.BAD_DATA);
    } catch (Exception e) {
      for (StackTraceElement element : e.getStackTrace()) {
        log.error(element.toString());
      }
      log.error("Exception on publishing. {}", e.getMessage());
      session.close(CloseStatus.SERVER_ERROR);
    }
  }

  private ChatSendMessage parseMessage(@NonNull TextMessage message) throws JsonProcessingException {
    return objectMapper.readValue(message.getPayload(), ChatSendMessage.class);
  }

  private ChatReceiveMessage saveAndConvert(Long placeId, ChatSendMessage chat) {
    if (chat.originChatId() == null) {
      final Chat originChat = chatService.saveChat(
          chat.userId(),
          placeId,
          chat.content()
      );

      return new ChatReceiveMessage(
          originChat.getId(),
          originChat.getUserId(),
          originChat.getPlaceId(),
          originChat.getContent(),
          null,
          originChat.getTimestamp()
      );
    }

    final Reply replyChat = chatService.saveReply(
        chat.userId(),
        chat.content(),
        chat.originChatId()
    );

    return new ChatReceiveMessage(
        replyChat.getId(),
        replyChat.getUserId(),
        placeId,
        replyChat.getContent(),
        chat.originChatId(),
        replyChat.getTimestamp()
    );
  }


  @Override
  public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws IOException {
    redisSubscriber.removeSession(session);
    session.close();
  }
}
