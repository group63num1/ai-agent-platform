// src/main/java/com/example/demo/controller/TeamInvitationController.java
package com.example.demo.controller;

import com.example.demo.app.entity.TeamInvitation;
import com.example.demo.common.ApiResponse;
import com.example.demo.service.TeamInvitationService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teams/invitations")
public class TeamInvitationController {

    @Autowired
    private TeamInvitationService teamInvitationService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (token != null) {
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 接受团队邀请
     */
    @PostMapping("/{invitationCode}/accept")
    public ApiResponse<Void> acceptInvitation(@PathVariable String invitationCode,
                                              HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "请先登录");
            }

            boolean success = teamInvitationService.acceptInvitation(invitationCode, currentUserId);
            if (success) {
                return new ApiResponse<>(0, "成功加入团队", null);
            } else {
                return ApiResponse.fail(400, "接受邀请失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 拒绝团队邀请
     */
    @PostMapping("/{invitationCode}/reject")
    public ApiResponse<Void> rejectInvitation(@PathVariable String invitationCode,
                                              HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "请先登录");
            }

            boolean success = teamInvitationService.rejectInvitation(invitationCode, currentUserId);
            if (success) {
                return new ApiResponse<>(0, "已拒绝邀请", null);
            } else {
                return ApiResponse.fail(400, "拒绝邀请失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取当前用户的待处理邀请
     */
    @GetMapping("/my-pending")
    public ApiResponse<List<TeamInvitation>> getMyPendingInvitations(HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            // 这里需要获取当前用户的邮箱来匹配邮箱邀请
            String userEmail = "user@example.com"; // 实际应该从用户信息获取

            List<TeamInvitation> invitations = teamInvitationService.getPendingInvitations(userEmail, currentUserId);
            return ApiResponse.ok(invitations);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取邀请详情
     */
    @GetMapping("/{invitationCode}")
    public ApiResponse<TeamInvitation> getInvitationDetail(@PathVariable String invitationCode) {
        try {
            TeamInvitation invitation = teamInvitationService.getInvitationByCode(invitationCode);
            if (invitation == null) {
                return ApiResponse.fail(404, "邀请不存在");
            }
            return ApiResponse.ok(invitation);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}