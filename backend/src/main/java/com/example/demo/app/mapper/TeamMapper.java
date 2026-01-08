// src/main/java/com/example/demo/app/mapper/TeamMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TeamMapper {

    int insert(Team team);

    Team selectById(@Param("id") Long id);

    Team selectByCode(@Param("code") String code);

    Team selectByName(@Param("name") String name);

    List<Team> selectAll();

    List<Team> selectByOwnerId(@Param("ownerId") Long ownerId);

    List<Team> selectByUserId(@Param("userId") Long userId);

    List<Team> selectByVisibility(@Param("visibility") String visibility);

    int update(Team team);

    int softDelete(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int countByOwnerId(@Param("ownerId") Long ownerId);
}