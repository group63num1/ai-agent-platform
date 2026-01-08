// src/main/java/com/example/demo/app/mapper/TeamMemberMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TeamMemberMapper {

    int insert(TeamMember teamMember);

    TeamMember selectById(@Param("id") Long id);

    TeamMember selectByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);

    List<TeamMember> selectByTeamId(@Param("teamId") Long teamId);

    List<TeamMember> selectByUserId(@Param("userId") Long userId);

    List<TeamMember> selectByTeamIdAndRole(@Param("teamId") Long teamId, @Param("role") String role);

    int updateRole(@Param("teamId") Long teamId, @Param("userId") Long userId, @Param("role") String role);

    int updateStatus(@Param("teamId") Long teamId, @Param("userId") Long userId, @Param("status") Integer status);

    int deleteByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);

    int deleteByTeamId(@Param("teamId") Long teamId);

    int countByTeamId(@Param("teamId") Long teamId);

    boolean existsByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);
}