package com.example.demo.app.mapper;

import com.example.demo.app.entity.AgentMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentMessageMapper {
    int insert(AgentMessage message);

    List<AgentMessage> selectRecentMessages(@Param("agentId") String agentId,
                                            @Param("sessionId") String sessionId,
                                            @Param("limit") int limit);

    List<AgentMessage> selectLatestMessages(@Param("agentId") String agentId,
                                            @Param("limit") int limit);
}


