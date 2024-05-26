package com.hat.hereandthere.chatservice.handler;

import com.hat.hereandthere.chatservice.utils.WebSocketSessionUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class PlaceChatHandler extends TextWebSocketHandler {

  private final RedisMessageSubscriber redisMessageSubscriber;
  private final RedisTemplate<String, Object> redisTemplate;


  public PlaceChatHandler(
      RedisMessageSubscriber redisMessageSubscriber,
      RedisTemplate<String, Object> redisTemplate
  ) {
    this.redisMessageSubscriber = redisMessageSubscriber;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    redisMessageSubscriber.addSession(session);
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
    redisMessageSubscriber.removeSession(session);
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
    final String channel = "place:" + WebSocketSessionUtils.getChannelId(session);
    redisTemplate.convertAndSend(channel, message.getPayload());
  }

  @Override
  public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
    redisMessageSubscriber.removeSession(session);
  }
}
