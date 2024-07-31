package com.hat.hereandthere.chatservice.chat;

import com.hat.hereandthere.chatservice.chat.dto.ChatResponseDto;
import com.hat.hereandthere.chatservice.chat.dto.ReplyResponseDto;
import com.hat.hereandthere.chatservice.chat.entity.Chat;
import com.hat.hereandthere.chatservice.chat.entity.Reply;

import java.util.*;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {

    final private ChatRepository repository;
    final private ChatRepositoryCustom repositoryCustom;
    final private ChatCountService chatCountService;

    public ChatService(
            ChatRepository repository,
            ChatRepositoryCustom repositoryCustom,
            ChatCountService chatCountService
    ) {
        this.repository = repository;
        this.repositoryCustom = repositoryCustom;
        this.chatCountService = chatCountService;
    }


    public Chat saveChat(
            @NonNull Long userId,
            @NonNull Long placeId,
            @NonNull String content
    ) {
        final Chat chat = Chat.builder()
                .userId(userId)
                .placeId(placeId)
                .content(content)
                .replies(List.of())
                .build();

        final Chat newChat = repository.save(chat);
        chatCountService.increaseChatCount(placeId);

        return newChat;
    }

    public Reply saveReply(
            @NonNull Long userId,
            @NonNull String content,
            @NonNull String originChatId
    ) {
        final Optional<Chat> optionalOriginChat = getChat(originChatId);

        if (optionalOriginChat.isPresent()) {
            final Chat originChat = optionalOriginChat.get();

            final Reply reply = Reply.builder()
                    .userId(userId)
                    .content(content)
                    .build();

            originChat.getReplies().add(reply);
            repository.save(originChat);

            return reply;
        }

        throw new RuntimeException();
    }

    public List<ChatResponseDto> getChats(@NonNull Long placeId, String lastChatId, int pageSize) {
        List<Chat> chatList;
        PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        if (lastChatId == null) {
            chatList = repository.findByPlaceIdOrderByIdDesc(placeId, pageRequest);
        } else {
            chatList = repository.findByPlaceIdAndIdBeforeOrderByIdDesc(placeId, lastChatId, pageRequest);
        }

        return chatList.stream().map(this::chatToDto).toList();
    }

    public Map<Long, Integer> getChatCount(@NonNull List<Long> placeIdList) {
        return repositoryCustom.countByPlaceIds(Set.copyOf(placeIdList));
    }

    private Optional<Chat> getChat(String id) {
        return repository.findById(id);
    }

    private ChatResponseDto chatToDto(Chat chat) {
        return new ChatResponseDto(
                chat.getId(),
                chat.getUserId(),
                chat.getContent(),
                chat.getTimestamp(),
                chat.getReplies().stream().map(this::replyToDto).toList()
        );
    }

    private ReplyResponseDto replyToDto(Reply reply) {
        return new ReplyResponseDto(
                reply.getId(),
                reply.getUserId(),
                reply.getContent(),
                reply.getTimestamp()
        );
    }
}
