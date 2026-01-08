package com.example.demo.app.mapper;

import com.example.demo.app.entity.Workflow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowMapper {
    int insert(Workflow workflow);

    int update(Workflow workflow);

    Workflow selectById(@Param("id") Long id);

    List<Workflow> selectByUserId(@Param("userId") Long userId);

    int deleteById(@Param("id") Long id);
}
