package com.hat.hereandthere.chatservice.handler;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession;

@Slf4j
public class PlaceChatHandler extends TextWebSocketHandler {


  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {

    final WebSocketServerSockJsSession webSocketServerSockJsSession = (WebSocketServerSockJsSession) session;
    session.sendMessage(new TextMessage("Connected well! placeID: " + getPlaceId(webSocketServerSockJsSession)));

  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
  }


  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
  }


  private long getPlaceId(@NonNull WebSocketSession session) {
    final URI uri = session.getUri();

    assert uri != null;
    return Long.parseLong(uri.getPath().split("/")[2]);
  }

}
