package com.hat.hereandthere.chatservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hat.hereandthere.chatservice.chat.ChatService;
import com.hat.hereandthere.chatservice.chat.dto.ChatReceiveMessage;
import com.hat.hereandthere.chatservice.chat.dto.ChatSendMessage;
import com.hat.hereandthere.chatservice.chat.entity.Chat;
import com.hat.hereandthere.chatservice.chat.entity.Reply;
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

  private final ChatService chatService;

  private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

  // 세션 저장용

  private final ObjectMapper objectMapper;


  public RedisMessageSubscriber(
      ChatService chatService,
      ObjectMapper objectMapper
  ) {
    this.chatService = chatService;
    this.objectMapper = objectMapper;
  }


  @Override
  public void onMessage(@NonNull Message message, byte[] pattern) {
    final ChatSendMessage chatSendMessage;
    final ChatReceiveMessage chatReceiveMessage;

    try {
      chatSendMessage = objectMapper.readValue(message.getBody(), ChatSendMessage.class);
      if (chatSendMessage.originChatId() == null) {
        final Chat chat = chatService.saveChat(
            chatSendMessage.userId(),
            getPlaceId(message),
            chatSendMessage.content()
        );

        chatReceiveMessage = new ChatReceiveMessage(
            chat.getId(),
            chat.getUserId(),
            chat.getPlaceId(),
            chat.getContent(),
            null,
            chat.getTimestamp()
        );
      } else {
        final Reply reply = chatService.saveReply(
            chatSendMessage.userId(),
            chatSendMessage.content(),
            chatSendMessage.originChatId()
        );

        chatReceiveMessage = new ChatReceiveMessage(
            reply.getId(),
            reply.getUserId(),
            getPlaceId(message),
            reply.getContent(),
            chatSendMessage.originChatId(),
            reply.getTimestamp()
        );
      }

      sendMessage(chatReceiveMessage, getActiveSessionSet(message));
    } catch (IOException e) {
      throw new RuntimeException(e);
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

  public boolean checkSession(WebSocketSession session) {
    final Long placeId = WebSocketSessionUtils.getChannelId(session);
    if (sessions.containsKey(placeId)) {
      return sessions.get(placeId).contains(session);
    }

    return false;
  }

  private Long getPlaceId(@NonNull Message message) {
    return Long.parseLong(new String(message.getChannel(), StandardCharsets.UTF_8).split(":")[1]);
  }

  private Set<WebSocketSession> getActiveSessionSet(@NonNull Message message) {
    return sessions.get(getPlaceId(message));
  }

  private void sendMessage(ChatReceiveMessage chatReceiveMessage, Set<WebSocketSession> targetSessionSet)
      throws IOException {
    final TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(chatReceiveMessage));

    for (WebSocketSession webSocketSession : targetSessionSet) {
      webSocketSession.sendMessage(textMessage);
    }
  }
}
