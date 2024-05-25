//package com.hat.hereandthere.chatservice.handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.hat.hereandthere.chatservice.chat.dto.ChatReceiveMessage;
//import com.hat.hereandthere.chatservice.chat.dto.ChatSendMessage;
//import com.hat.hereandthere.chatservice.utils.WebSocketSessionUtils;
//import jakarta.annotation.PostConstruct;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//
//@Slf4j
//@Component
//public class RedisMessageSubscriber extends MessageListenerAdapter {
//
//  // <PlaceId, S
//  private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
//
//  private final RedisTemplate<String, Object> redisTemplate;
//
//  private final ObjectMapper objectMapper = new ObjectMapper();
//
//
//  public RedisMessageSubscriber(RedisTemplate<String, Object> redisTemplate) {
//    this.redisTemplate = redisTemplate;
//    this.objectMapper.registerModule(new JavaTimeModule());
//  }
//
//
//  @PostConstruct
//  public void init() {
//  }
//
//  @Override
//  public void onMessage(@NonNull Message message, byte[] pattern) {
//    final ChatSendMessage chatSendMessage;
//    try {
//      chatSendMessage = objectMapper.readValue(message.getBody(), ChatSendMessage.class);
//
//      final ChatReceiveMessage chatReceiveMessage = new ChatReceiveMessage(
//          UUID.randomUUID().toString(),
//          chatSendMessage.userId(),
//          getPlaceId(message),
//          chatSendMessage.content(),
//          chatSendMessage.originChatId(),
//          OffsetDateTime.now(ZoneOffset.UTC)
//      );
//      sendMessage(chatReceiveMessage, getActiveSessionSet(message));
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  public void addSession(WebSocketSession session) {
//    final Long placeId = WebSocketSessionUtils.getChannelId(session);
//
//    if (sessions.containsKey(placeId)) {
//      sessions.computeIfPresent(placeId, (key, value) -> {
//        value.add(session);
//
//        return value;
//      });
//    } else {
//      sessions.put(placeId, new HashSet<>(Collections.singletonList(session)));
//    }
//  }
//
//  public void removeSession(WebSocketSession session) {
//    final Long placeId = WebSocketSessionUtils.getChannelId(session);
//    sessions.computeIfPresent(placeId, (key, value) -> {
//      value.remove(session);
//      return value;
//    });
//  }
//
//  private Long getPlaceId(@NonNull Message message) {
//    return Long.parseLong(new String(message.getChannel(), StandardCharsets.UTF_8).split(":")[1]);
//  }
//
//  private Set<WebSocketSession> getActiveSessionSet(@NonNull Message message) {
//    return sessions.get(getPlaceId(message));
//  }
//
//  private void sendMessage(ChatReceiveMessage chatReceiveMessage, Set<WebSocketSession> targetSessionSet) {
//    targetSessionSet.forEach(webSocketSession -> {
//      try {
//        webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatReceiveMessage)));
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    });
//  }
//}
