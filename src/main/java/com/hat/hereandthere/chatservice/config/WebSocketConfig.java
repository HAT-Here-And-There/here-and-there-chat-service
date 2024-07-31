package com.hat.hereandthere.chatservice.config;

import com.hat.hereandthere.chatservice.handler.PlaceChatHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final PlaceChatHandler placeChatHandler;


  public WebSocketConfig(
      PlaceChatHandler placeChatHandler
  ) {
    this.placeChatHandler = placeChatHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

    registry.addHandler(placeChatHandler, "/place/*")
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }
}

