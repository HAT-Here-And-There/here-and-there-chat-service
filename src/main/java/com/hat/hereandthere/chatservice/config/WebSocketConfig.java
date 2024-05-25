package com.hat.hereandthere.chatservice.config;

import com.hat.hereandthere.chatservice.handler.PlaceChatHandler;
//import com.hat.hereandthere.chatservice.handler.RedisMessageSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final PlaceChatHandler placeChatHandler;

//  private final RedisMessageSubscriber redisMessageSubscriber;
//  private final RedisTemplate<String, Object> redisTemplate;

  public WebSocketConfig(
      PlaceChatHandler placeChatHandler
//      RedisMessageSubscriber redisMessageSubscriber,
//      RedisTemplate<String, Object> redisTemplate
  ) {
    this.placeChatHandler = placeChatHandler;

//    this.redisMessageSubscriber = redisMessageSubscriber;
//    this.redisTemplate = redisTemplate;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

    registry.addHandler(placeChatHandler, "/place/*")
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }


}

