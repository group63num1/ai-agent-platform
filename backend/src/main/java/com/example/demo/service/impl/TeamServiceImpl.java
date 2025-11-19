// src/main/java/com/example/demo/service/impl/TeamServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.Team;
import com.example.demo.app.entity.TeamMember;
import com.example.demo.app.mapper.TeamMapper;
import com.example.demo.app.mapper.TeamMemberMapper;
import com.example.demo.dto.TeamCreateDTO;
import com.example.demo.dto.TeamDTO;
import com.example.demo.dto.TeamMemberDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.TeamService;
import com.example.demo.service.UserService;
import com.example.demo.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public TeamDTO createTeam(TeamCreateDTO teamCreateDTO, Long ownerId) {
        // 检查团队名称是否已存在
        Team existingTeam = teamMapper.selectByName(teamCreateDTO.getName());
        if (existingTeam != null) {
            throw new RuntimeException("团队名称已存在");
        }

        // 创建团队
        Team team = new Team();
        team.setName(teamCreateDTO.getName());
        team.setCode(generateTeamCode());
        team.setDescription(teamCreateDTO.getDescription());
        team.setVisibility(teamCreateDTO.getVisibility());
        team.setJoinPolicy(teamCreateDTO.getJoinPolicy());
        team.setMaxMembers(teamCreateDTO.getMaxMembers());
        team.setOwnerId(ownerId);
        team.setStatus(1); // 正常状态

        int result = teamMapper.insert(team);
        if (result <= 0) {
            throw new RuntimeException("创建团队失败");
        }

        // 添加创建者为团队负责人
        addMember(team.getId(), ownerId, "owner", ownerId);

        return getTeamById(team.getId());
    }

    @Override
    public TeamDTO getTeamById(Long id) {
        Team team = teamMapper.selectById(id);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }
        return convertToTeamDTO(team);
    }

    @Override
    public TeamDTO getTeamByCode(String code) {
        Team team = teamMapper.selectByCode(code);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }
        return convertToTeamDTO(team);
    }

    @Override
    public List<TeamDTO> getAllTeams() {
        List<Team> teams = teamMapper.selectAll();
        return BeanConvertUtil.convertList(teams, TeamDTO.class);
    }

    @Override
    public List<TeamDTO> getTeamsByOwner(Long ownerId) {
        List<Team> teams = teamMapper.selectByOwnerId(ownerId);
        return BeanConvertUtil.convertList(teams, TeamDTO.class);
    }

    @Override
    public List<TeamDTO> getTeamsByUser(Long userId) {
        List<Team> teams = teamMapper.selectByUserId(userId);
        return BeanConvertUtil.convertList(teams, TeamDTO.class);
    }

    @Override
    @Transactional
    public TeamDTO updateTeam(Long id, TeamDTO teamDTO, Long currentUserId) {
        Team existingTeam = teamMapper.selectById(id);
        if (existingTeam == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查权限：只有团队负责人可以修改团队信息
        if (!isTeamOwner(id, currentUserId)) {
            throw new RuntimeException("没有权限修改团队信息");
        }

        existingTeam.setName(teamDTO.getName());
        existingTeam.setDescription(teamDTO.getDescription());
        existingTeam.setVisibility(teamDTO.getVisibility());
        existingTeam.setJoinPolicy(teamDTO.getJoinPolicy());
        existingTeam.setMaxMembers(teamDTO.getMaxMembers());

        int result = teamMapper.update(existingTeam);
        if (result <= 0) {
            throw new RuntimeException("更新团队失败");
        }

        return getTeamById(id);
    }

    @Override
    @Transactional
    public void deleteTeam(Long id, Long currentUserId) {
        Team team = teamMapper.selectById(id);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查权限：只有团队负责人可以删除团队
        if (!isTeamOwner(id, currentUserId)) {
            throw new RuntimeException("没有权限删除团队");
        }

        int result = teamMapper.softDelete(id);
        if (result <= 0) {
            throw new RuntimeException("删除团队失败");
        }
    }

    @Override
    @Transactional
    public TeamMemberDTO addMember(Long teamId, Long userId, String role, Long invitedBy) {
        // 检查团队是否存在
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查用户是否已经是团队成员
        if (teamMemberMapper.existsByTeamAndUser(teamId, userId)) {
            throw new RuntimeException("用户已是团队成员");
        }

        // 检查团队人数限制
        int currentMemberCount = teamMemberMapper.countByTeamId(teamId);
        if (team.getMaxMembers() != null && currentMemberCount >= team.getMaxMembers()) {
            throw new RuntimeException("团队人数已达上限");
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setTeamId(teamId);
        teamMember.setUserId(userId);
        teamMember.setRole(role);
        teamMember.setStatus(1); // 正常状态
        teamMember.setInvitedBy(invitedBy);

        int result = teamMemberMapper.insert(teamMember);
        if (result <= 0) {
            throw new RuntimeException("添加成员失败");
        }

        return getTeamMember(teamId, userId);
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId, Long currentUserId) {
        // 检查权限：团队负责人或管理员可以移除成员，成员可以自己退出
        if (!isTeamOwner(teamId, currentUserId) &&
                !isTeamAdmin(teamId, currentUserId) &&
                !userId.equals(currentUserId)) {
            throw new RuntimeException("没有权限移除成员");
        }

        // 团队负责人不能移除自己
        if (isTeamOwner(teamId, userId) && userId.equals(currentUserId)) {
            throw new RuntimeException("团队负责人不能移除自己");
        }

        int result = teamMemberMapper.deleteByTeamAndUser(teamId, userId);
        if (result <= 0) {
            throw new RuntimeException("移除成员失败");
        }
    }

    @Override
    @Transactional
    public TeamMemberDTO updateMemberRole(Long teamId, Long userId, String role, Long currentUserId) {
        // 检查权限：只有团队负责人可以修改成员角色
        if (!isTeamOwner(teamId, currentUserId)) {
            throw new RuntimeException("没有权限修改成员角色");
        }

        // 团队负责人不能修改自己的角色
        if (isTeamOwner(teamId, userId) && userId.equals(currentUserId)) {
            throw new RuntimeException("不能修改自己的角色");
        }

        int result = teamMemberMapper.updateRole(teamId, userId, role);
        if (result <= 0) {
            throw new RuntimeException("更新成员角色失败");
        }

        return getTeamMember(teamId, userId);
    }

    @Override
    public List<TeamMemberDTO> getTeamMembers(Long teamId) {
        List<TeamMember> members = teamMemberMapper.selectByTeamId(teamId);
        return BeanConvertUtil.convertList(members, TeamMemberDTO.class);
    }

    @Override
    public List<TeamMemberDTO> getUserTeams(Long userId) {
        List<TeamMember> members = teamMemberMapper.selectByUserId(userId);
        return BeanConvertUtil.convertList(members, TeamMemberDTO.class);
    }

    @Override
    public boolean isTeamMember(Long teamId, Long userId) {
        return teamMemberMapper.existsByTeamAndUser(teamId, userId);
    }

    @Override
    public boolean isTeamOwner(Long teamId, Long userId) {
        TeamMember member = teamMemberMapper.selectByTeamAndUser(teamId, userId);
        return member != null && "owner".equals(member.getRole());
    }

    @Override
    public boolean isTeamAdmin(Long teamId, Long userId) {
        TeamMember member = teamMemberMapper.selectByTeamAndUser(teamId, userId);
        return member != null && ("owner".equals(member.getRole()) || "admin".equals(member.getRole()));
    }

    private TeamDTO convertToTeamDTO(Team team) {
        TeamDTO teamDTO = BeanConvertUtil.convert(team, TeamDTO.class);

        // 设置团队负责人信息
        try {
            UserDTO owner = userService.getUserById(team.getOwnerId());
            teamDTO.setOwner(owner);
        } catch (Exception e) {
            // 忽略获取用户信息失败的情况
        }

        // 设置成员数量
        int memberCount = teamMemberMapper.countByTeamId(team.getId());
        teamDTO.setMemberCount(memberCount);

        return teamDTO;
    }

    private TeamMemberDTO getTeamMember(Long teamId, Long userId) {
        TeamMember member = teamMemberMapper.selectByTeamAndUser(teamId, userId);
        if (member == null) {
            throw new RuntimeException("团队成员不存在");
        }
        return BeanConvertUtil.convert(member, TeamMemberDTO.class);
    }

    private String generateTeamCode() {
        return "TEAM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
