package com.hat.hereandthere.chatservice.chat.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Builder()
@Data()
@ToString()
@Document(collection = "chat")
public class Chat {

  @Id
  private String id;

  @Field("userId")
  private Long userId;

  @Indexed(name = "placeId")
  @Field("placeId")
  private Long placeId;

  @Field("content")
  private String content;

  @CreatedDate
  @Field("timestamp")
  private LocalDateTime timestamp;

  @Field("replies")
  private List<Reply> replies;
}
