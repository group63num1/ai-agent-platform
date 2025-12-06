package com.example.demo.app.mapper;

import com.example.demo.app.entity.AgentSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentSessionMapper {

    int insert(AgentSession session);

    AgentSession selectById(@Param("sessionId") String sessionId);

    List<AgentSession> selectByAgentId(@Param("agentId") String agentId);

    int deleteById(@Param("sessionId") String sessionId);
}

