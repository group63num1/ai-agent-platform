// src/main/java/com/example/demo/app/mapper/ChatSessionMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChatSessionMapper {

    int insert(ChatSession chatSession);

    ChatSession selectById(@Param("id") Long id);

    List<ChatSession> selectByUserId(@Param("userId") Long userId, @Param("includeDeleted") Boolean includeDeleted);

    int update(ChatSession chatSession);

    int softDelete(@Param("id") Long id);

    int updateTitle(@Param("id") Long id, @Param("title") String title);
}

