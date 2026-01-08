package com.example.demo.app.mapper;

import com.example.demo.app.entity.Plugin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PluginMapper {
    int insert(Plugin plugin);

    int update(Plugin plugin);

    Plugin selectById(@Param("id") Long id);

    Plugin selectByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    List<Plugin> selectAll();

    int delete(@Param("id") Long id);

    List<String> selectPublishedNames();
}





