package com.hat.hereandthere.chatservice.chat.entity;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder()
@Data()
@Document()
public class Reply {

  @Id
  private String id;

  @Field("userId")
  private Long userId;

  @Field("content")
  private String content;

  @Field("timestamp")
  private LocalDateTime timestamp;


}
