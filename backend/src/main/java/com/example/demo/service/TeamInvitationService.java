// src/main/java/com/example/demo/service/TeamInvitationService.java
package com.example.demo.service;

import com.example.demo.app.entity.TeamInvitation;
import java.util.List;

public interface TeamInvitationService {

    // 创建团队邀请
    TeamInvitation createInvitation(Long teamId, Long inviterId, String inviteeEmail,
                                    Long inviteeUserId, String role, String message);
    // 在 TeamInvitationService.java 中添加缺失的方法
    /**
     * 根据邀请码获取邀请详情
     */
    TeamInvitation getInvitationByCode(String invitationCode);

    // 通过邀请码接受邀请
    boolean acceptInvitation(String invitationCode, Long currentUserId);

    // 拒绝邀请
    boolean rejectInvitation(String invitationCode, Long currentUserId);

    // 获取用户的待处理邀请
    List<TeamInvitation> getPendingInvitations(String email, Long userId);

    // 获取团队的邀请列表
    List<TeamInvitation> getTeamInvitations(Long teamId);

    // 取消邀请
    boolean cancelInvitation(Long invitationId, Long currentUserId);

    // 重新发送邀请
    boolean resendInvitation(Long invitationId, Long currentUserId);
    // 在 TeamInvitationService.java 中添加缺失的方法


    // 清理过期邀请
    int cleanupExpiredInvitations();
}