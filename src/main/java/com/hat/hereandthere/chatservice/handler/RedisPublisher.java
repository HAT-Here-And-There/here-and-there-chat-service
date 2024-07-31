package com.hat.hereandthere.chatservice.handler;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisPublisher {
  private final RedisTemplate<String, String> redisTemplate;

  public RedisPublisher(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  void publish(String channel, Object message) {
    redisTemplate.convertAndSend(channel, message);
  }

}
