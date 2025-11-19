// src/main/java/com/example/demo/service/TeamService.java
package com.example.demo.service;

import com.example.demo.dto.TeamCreateDTO;
import com.example.demo.dto.TeamDTO;
import com.example.demo.dto.TeamMemberDTO;
import java.util.List;

public interface TeamService {

    // 团队管理
    TeamDTO createTeam(TeamCreateDTO teamCreateDTO, Long ownerId);
    TeamDTO getTeamById(Long id);
    TeamDTO getTeamByCode(String code);
    List<TeamDTO> getAllTeams();
    List<TeamDTO> getTeamsByOwner(Long ownerId);
    List<TeamDTO> getTeamsByUser(Long userId);
    TeamDTO updateTeam(Long id, TeamDTO teamDTO, Long currentUserId);
    void deleteTeam(Long id, Long currentUserId);

    // 成员管理
    TeamMemberDTO addMember(Long teamId, Long userId, String role, Long invitedBy);
    void removeMember(Long teamId, Long userId, Long currentUserId);
    TeamMemberDTO updateMemberRole(Long teamId, Long userId, String role, Long currentUserId);
    List<TeamMemberDTO> getTeamMembers(Long teamId);
    List<TeamMemberDTO> getUserTeams(Long userId);
    boolean isTeamMember(Long teamId, Long userId);
    boolean isTeamOwner(Long teamId, Long userId);
    boolean isTeamAdmin(Long teamId, Long userId);
}
