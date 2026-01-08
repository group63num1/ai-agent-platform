package com.example.demo.app.mapper;

import com.example.demo.app.entity.Agent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AgentMapper {
    int insert(Agent agent);

    Agent selectById(@Param("id") String id);

    Agent selectByName(@Param("name") String name);

    List<Agent> selectPage(@Param("keyword") String keyword,
                           @Param("offset") int offset,
                           @Param("pageSize") int pageSize);

    long countByKeyword(@Param("keyword") String keyword);

    List<Agent> selectByStatus(@Param("status") String status);

    int update(Agent agent);

    int delete(@Param("id") String id);

    int updateStatus(@Param("id") String id, @Param("status") String status);
}



