package com.hat.hereandthere.chatservice.utils;

import java.net.URI;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketSessionUtils {

  private WebSocketSessionUtils() throws IllegalAccessException {
    throw new IllegalAccessException("Util class cannot be instantiated");
  }

  public static Long getChannelId(
     @NonNull WebSocketSession session
  ) {
    final URI uri = session.getUri();

    assert uri != null;
    return Long.parseLong(uri.getPath().split("/")[2]);
  }

}
