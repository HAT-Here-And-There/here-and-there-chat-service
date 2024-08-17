package com.hat.hereandthere.chatservice.config;

import com.hat.hereandthere.chatservice.chat.entity.Chat;

import java.time.LocalDateTime;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@EnableMongoRepositories(basePackages = "com.hat.hereandthere.chatservice")
@EnableMongoAuditing
@Configuration
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    private final MongoMappingContext mongoMappingContext;


    public MongoConfig(MongoMappingContext mongoMappingContext) {
        this.mongoMappingContext = mongoMappingContext;
    }

    @Bean
    public MappingMongoConverter reactiveMappingMongoConverter() {
        MappingMongoConverter converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE,
                mongoMappingContext);

        // _class 필드 제거
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

    @Bean
    public AbstractMongoEventListener<Chat> replyMongoEventListener() {
        return new AbstractMongoEventListener<>() {
            @Override
            public void onBeforeConvert(@NonNull BeforeConvertEvent<Chat> event) {

                Chat updatedChat = event.getSource();

                updatedChat.getReplies().stream()
                        .filter((e) -> e.getId() == null)
                        .forEach(e -> {
                            e.setId(new ObjectId().toString());
                            e.setTimestamp(LocalDateTime.now());
                        });
            }
        };
    }

    @Bean
    MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "here_and_there_chat");
    }

    @Bean
    MongoClient mongoClient() {
        System.out.println(mongoUri);
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MappingMongoConverter mappingMongoConverter) {
        return new MongoTemplate(mongoDatabaseFactory, mappingMongoConverter);
    }
}
