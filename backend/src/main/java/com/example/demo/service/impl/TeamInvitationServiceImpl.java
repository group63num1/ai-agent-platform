// src/main/java/com/example/demo/service/impl/TeamInvitationServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.TeamInvitation;
import com.example.demo.app.mapper.TeamInvitationMapper;
import com.example.demo.app.mapper.TeamMapper;
import com.example.demo.app.mapper.TeamMemberMapper;
import com.example.demo.service.TeamInvitationService;
import com.example.demo.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TeamInvitationServiceImpl implements TeamInvitationService {

    @Autowired
    private TeamInvitationMapper teamInvitationMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;
    // 在 TeamInvitationServiceImpl.java 中添加
    @Override
    public TeamInvitation getInvitationByCode(String invitationCode) {
        return teamInvitationMapper.selectByInvitationCode(invitationCode);
    }

    // 邀请有效期：7天
    private static final int INVITATION_EXPIRE_DAYS = 7;

    @Override
    @Transactional
    public TeamInvitation createInvitation(Long teamId, Long inviterId, String inviteeEmail,
                                           Long inviteeUserId, String role, String message) {
        // 验证团队存在且邀请者有权限
        var team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }

        // 检查邀请者是否是团队管理员或负责人
        var inviterMember = teamMemberMapper.selectByTeamAndUser(teamId, inviterId);
        if (inviterMember == null ||
                !("owner".equals(inviterMember.getRole()) || "admin".equals(inviterMember.getRole()))) {
            throw new RuntimeException("没有权限邀请成员");
        }

        // 检查被邀请者是否已经是团队成员
        if (inviteeUserId != null) {
            boolean isAlreadyMember = teamMemberMapper.existsByTeamAndUser(teamId, inviteeUserId);
            if (isAlreadyMember) {
                throw new RuntimeException("用户已是团队成员");
            }
        }

        // 生成唯一邀请码
        String invitationCode = generateInvitationCode();

        // 创建邀请记录
        TeamInvitation invitation = new TeamInvitation();
        invitation.setTeamId(teamId);
        invitation.setInviterId(inviterId);
        invitation.setInviteeEmail(inviteeEmail);
        invitation.setInviteeUserId(inviteeUserId);
        invitation.setInvitationCode(invitationCode);
        invitation.setRole(role != null ? role : "member");
        invitation.setMessage(message);
        invitation.setStatus("pending");
        invitation.setExpiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRE_DAYS));
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());

        teamInvitationMapper.insert(invitation);

        // 如果通过邮箱邀请，发送邀请邮件
        if (inviteeEmail != null && !inviteeEmail.trim().isEmpty()) {
            sendInvitationEmail(inviteeEmail, team.getName(), inviterId, invitationCode);
        }

        return invitation;
    }

    @Override
    @Transactional
    public boolean acceptInvitation(String invitationCode, Long currentUserId) {
        TeamInvitation invitation = teamInvitationMapper.selectByInvitationCode(invitationCode);

        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        if (!"pending".equals(invitation.getStatus())) {
            throw new RuntimeException("邀请已处理");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("邀请已过期");
        }

        // 验证当前用户是否有权接受此邀请
        if (invitation.getInviteeUserId() != null && !invitation.getInviteeUserId().equals(currentUserId)) {
            throw new RuntimeException("无权接受此邀请");
        }

        // 如果邀请是通过邮箱，需要验证邮箱匹配
        if (invitation.getInviteeEmail() != null) {
            // 这里可以添加邮箱验证逻辑
            // 比如检查当前用户的邮箱是否与邀请邮箱匹配
        }

        // 添加用户到团队
        teamMemberMapper.insert(createTeamMember(invitation, currentUserId));

        // 更新邀请状态
        teamInvitationMapper.updateStatus(invitation.getId(), "accepted", LocalDateTime.now());

        return true;
    }

    @Override
    @Transactional
    public boolean rejectInvitation(String invitationCode, Long currentUserId) {
        TeamInvitation invitation = teamInvitationMapper.selectByInvitationCode(invitationCode);

        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        if (!"pending".equals(invitation.getStatus())) {
            throw new RuntimeException("邀请已处理");
        }

        // 验证权限
        if (invitation.getInviteeUserId() != null && !invitation.getInviteeUserId().equals(currentUserId)) {
            throw new RuntimeException("无权拒绝此邀请");
        }

        // 更新邀请状态
        teamInvitationMapper.updateStatus(invitation.getId(), "rejected", LocalDateTime.now());

        return true;
    }

    @Override
    public List<TeamInvitation> getPendingInvitations(String email, Long userId) {
        return teamInvitationMapper.selectPendingInvitations(email, userId);
    }

    @Override
    public List<TeamInvitation> getTeamInvitations(Long teamId) {
        return teamInvitationMapper.selectByTeamId(teamId);
    }

    @Override
    @Transactional
    public boolean cancelInvitation(Long invitationId, Long currentUserId) {
        TeamInvitation invitation = teamInvitationMapper.selectById(invitationId);

        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        // 验证权限：只有邀请者或团队管理员可以取消
        var teamMember = teamMemberMapper.selectByTeamAndUser(invitation.getTeamId(), currentUserId);
        if (teamMember == null ||
                !("owner".equals(teamMember.getRole()) || "admin".equals(teamMember.getRole())) ||
                !invitation.getInviterId().equals(currentUserId)) {
            throw new RuntimeException("没有权限取消邀请");
        }

        // 删除邀请
        teamInvitationMapper.deleteByTeamId(invitation.getTeamId());
        return true;
    }

    @Override
    public boolean resendInvitation(Long invitationId, Long currentUserId) {
        TeamInvitation invitation = teamInvitationMapper.selectById(invitationId);

        if (invitation == null) {
            throw new RuntimeException("邀请不存在");
        }

        // 验证权限
        if (!invitation.getInviterId().equals(currentUserId)) {
            throw new RuntimeException("没有权限重新发送邀请");
        }

        // 重新发送邮件
        if (invitation.getInviteeEmail() != null) {
            sendInvitationEmail(invitation.getInviteeEmail(),
                    teamMapper.selectById(invitation.getTeamId()).getName(),
                    currentUserId, invitation.getInvitationCode());
        }

        return true;
    }

    @Override
    public int cleanupExpiredInvitations() {
        return teamInvitationMapper.expireOldInvitations(LocalDateTime.now());
    }

    /**
     * 生成唯一邀请码
     */
    private String generateInvitationCode() {
        return "INV_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 发送邀请邮件
     */
    private void sendInvitationEmail(String email, String teamName, Long inviterId, String invitationCode) {
        try {
            String subject = "团队邀请 - " + teamName;
            String content = "您被邀请加入团队: " + teamName + "\n\n" +
                    "邀请链接: http://localhost:28080/api/v1/teams/invitations/" + invitationCode + "\n\n" +
                    "请点击链接接受邀请。\n\n" +
                    "DevOps用户管理系统团队";

            // 这里可以调用邮件服务发送真实邮件
            System.out.println("发送团队邀请邮件到 " + email + ": " + content);

        } catch (Exception e) {
            System.err.println("发送邀请邮件失败: " + e.getMessage());
        }
    }

    /**
     * 创建团队成员
     */
    private com.example.demo.app.entity.TeamMember createTeamMember(TeamInvitation invitation, Long userId) {
        com.example.demo.app.entity.TeamMember member = new com.example.demo.app.entity.TeamMember();
        member.setTeamId(invitation.getTeamId());
        member.setUserId(userId);
        member.setRole(invitation.getRole());
        member.setStatus(1);
        member.setJoinedAt(LocalDateTime.now());
        member.setInvitedBy(invitation.getInviterId());
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        return member;
    }
}