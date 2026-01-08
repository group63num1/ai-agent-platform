// src/main/java/com/example/demo/app/mapper/TeamInvitationMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.TeamInvitation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TeamInvitationMapper {

    int insert(TeamInvitation teamInvitation);

    TeamInvitation selectById(@Param("id") Long id);

    TeamInvitation selectByInvitationCode(@Param("invitationCode") String invitationCode);

    List<TeamInvitation> selectByTeamId(@Param("teamId") Long teamId);

    List<TeamInvitation> selectByInviterId(@Param("inviterId") Long inviterId);

    List<TeamInvitation> selectByInviteeEmail(@Param("inviteeEmail") String inviteeEmail);

    List<TeamInvitation> selectByInviteeUserId(@Param("inviteeUserId") Long inviteeUserId);

    List<TeamInvitation> selectPendingInvitations(@Param("inviteeEmail") String inviteeEmail,
                                                  @Param("inviteeUserId") Long inviteeUserId);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("respondedAt") LocalDateTime respondedAt);

    int expireOldInvitations(@Param("currentTime") LocalDateTime currentTime);

    int deleteByTeamId(@Param("teamId") Long teamId);
}