// src/main/java/com/example/demo/app/mapper/ChatMessageMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessage chatMessage);

    ChatMessage selectById(@Param("id") Long id);

    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId);

    List<ChatMessage> selectBySessionIdWithPagination(
            @Param("sessionId") Long sessionId,
            @Param("lastMessageId") Long lastMessageId,
            @Param("limit") Integer limit
    );

    int deleteBySessionId(@Param("sessionId") Long sessionId);

    int countBySessionId(@Param("sessionId") Long sessionId);
}

