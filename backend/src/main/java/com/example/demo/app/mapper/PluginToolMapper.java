package com.example.demo.app.mapper;

import com.example.demo.app.entity.PluginTool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginToolMapper {
    int insert(PluginTool tool);

    int update(PluginTool tool);

    PluginTool selectById(@Param("id") Long id);

    List<PluginTool> selectByPluginId(@Param("pluginId") Long pluginId);

    int deleteByPluginId(@Param("pluginId") Long pluginId);
}


