package org.example.redpic.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.redpic.dto.RedisMessageDto;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisChatMemoryRepository implements ChatMemoryRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper=new ObjectMapper();
    private static final String KEY_PREFIX="redpic:chat:memory:";

    public RedisChatMemoryRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        String key=KEY_PREFIX+conversationId;
        String json=redisTemplate.opsForValue().get(key);
        if(json==null){
            return new ArrayList<>();
        }
        try {
            List<RedisMessageDto> dtos=objectMapper.readValue(json, new TypeReference<List<RedisMessageDto>>() {});
            return dtos.stream().map(dto->{if ("user".equals(dto.getRole())){
                return new UserMessage(dto.getContent());
            }else{
                return new AssistantMessage(dto.getContent());
            }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("反序列化记忆失败"+e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String key=KEY_PREFIX+conversationId;
        try {
            List<RedisMessageDto>dtos=messages.stream().map(message->{
                String role=message instanceof UserMessage?"user":"assistant";
                return new RedisMessageDto(role,message.getText());
            }).collect(Collectors.toList());
            String json=objectMapper.writeValueAsString(dtos);
            redisTemplate.opsForValue().set(key,json,30, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("保存到记忆失败",e);
        }

    }

    @Override
    public void deleteByConversationId(String conversationId) {
        String key=KEY_PREFIX+conversationId;
        redisTemplate.delete(key);

    }
}
