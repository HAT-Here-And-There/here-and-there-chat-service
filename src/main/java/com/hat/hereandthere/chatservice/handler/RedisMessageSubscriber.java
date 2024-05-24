package com.hat.hereandthere.chatservice.handler;

import com.hat.hereandthere.chatservice.utils.WebSocketSessionUtils;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class RedisMessageSubscriber extends MessageListenerAdapter {

  // <PlaceId, S
  private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisMessageSubscriber(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;

  }


  @PostConstruct
  public void init() {
  }

  @Override
  public void onMessage(@NonNull Message message, byte[] pattern) {
    log.warn("    RedisMessageSubscriber.onMessage message: {}", message);
    log.warn("    RedisMessageSubscriber.onMessage message.getBody: {}", message.getBody());
    log.warn("    RedisMessageSubscriber.onMessage message.getChannel: {}", message.getChannel());

    final Long placeId = Long.parseLong(new String(message.getChannel(), StandardCharsets.UTF_8).split(":")[1]);
    final Set<WebSocketSession> activeSessions = sessions.get(placeId);
    activeSessions.forEach(webSocketSession -> {
      try {
        webSocketSession.sendMessage(new TextMessage(message.getBody()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

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

}
