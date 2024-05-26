package com.hat.hereandthere.chatservice.chat;

import com.hat.hereandthere.chatservice.chat.entity.Chat;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

  @Query("{ 'placeId' : ?0, id : { $lt : { $oid : ?1 } } } ")
  List<Chat> findByPlaceIdAndIdBeforeOrderByIdDesc(Long placeId, String id, Pageable pageable);

  List<Chat> findByPlaceIdOrderByIdDesc(Long placeId, Pageable pageable);

}
