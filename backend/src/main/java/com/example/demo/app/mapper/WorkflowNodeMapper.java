package com.example.demo.app.mapper;

import com.example.demo.app.entity.WorkflowNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowNodeMapper {
    int insert(WorkflowNode node);

    int deleteByWorkflowId(@Param("workflowId") Long workflowId);

    List<WorkflowNode> selectByWorkflowId(@Param("workflowId") Long workflowId);
}
