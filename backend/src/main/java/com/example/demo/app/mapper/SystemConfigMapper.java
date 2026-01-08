// src/main/java/com/example/demo/app/mapper/SystemConfigMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SystemConfigMapper {

    int insert(SystemConfig systemConfig);

    SystemConfig selectById(@Param("id") Long id);

    SystemConfig selectByKey(@Param("configKey") String configKey);

    List<SystemConfig> selectAll();

    List<SystemConfig> selectByIsPublic(@Param("isPublic") Integer isPublic);

    int update(SystemConfig systemConfig);

    int updateValueByKey(@Param("configKey") String configKey, @Param("configValue") String configValue);

    int delete(@Param("id") Long id);

    int deleteByKey(@Param("configKey") String configKey);
}