package com.hat.hereandthere.chatservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hat.hereandthere.chatservice.chat.dto.ChatReceiveMessage;
import com.hat.hereandthere.chatservice.utils.WebSocketSessionUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class RedisMessageSubscriber extends MessageListenerAdapter {

  private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;


  public RedisMessageSubscriber(
      ObjectMapper objectMapper
  ) {
    this.objectMapper = objectMapper;
  }


  @Override
  public void onMessage(@NonNull Message message, byte[] pattern) {
    try {
      final ChatReceiveMessage chatReceiveMessage = objectMapper.readValue(message.getBody(),
          ChatReceiveMessage.class);
      sendToSessions(chatReceiveMessage, getActiveSessionSet(message));
    } catch (Exception e) {
      log.error("Exception on parsing. {}", e.getMessage());
    }
  }

  public void addSession(WebSocketSession session) {
    final Long placeId = WebSocketSessionUtils.getChannelId(session);

    if (sessions.containsKey(placeId)) {
      sessions.computeIfPresent(placeId, (key, value) -> {
        value.add(session);

        return value;
      });
    } else {
      sessions.put(placeId, new HashSet<>(Collections.singletonList(session)));
    }
  }

  public void removeSession(WebSocketSession session) {
    final Long placeId = WebSocketSessionUtils.getChannelId(session);
    sessions.computeIfPresent(placeId, (key, value) -> {
      value.remove(session);
      return value;
    });
  }

  private Long getPlaceId(@NonNull Message message) {
    return Long.parseLong(new String(message.getChannel(), StandardCharsets.UTF_8).split(":")[1]);
  }

  private Set<WebSocketSession> getActiveSessionSet(@NonNull Message message) {
    return sessions.get(getPlaceId(message));
  }

  private void sendToSessions(ChatReceiveMessage chatReceiveMessage, Set<WebSocketSession> targetSessionSet)
      throws IOException {
    final TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(chatReceiveMessage));

    for (WebSocketSession webSocketSession : targetSessionSet) {
      webSocketSession.sendMessage(textMessage);
    }
  }
}
