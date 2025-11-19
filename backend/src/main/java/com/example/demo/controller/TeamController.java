// src/main/java/com/example/demo/controller/TeamController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.TeamCreateDTO;
import com.example.demo.dto.TeamDTO;
import com.example.demo.dto.TeamMemberDTO;
import com.example.demo.app.entity.TeamInvitation;
import com.example.demo.service.TeamService;
import com.example.demo.service.TeamInvitationService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

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
     * 创建团队
     */
    @PostMapping
    public ApiResponse<TeamDTO> createTeam(@Valid @RequestBody TeamCreateDTO teamCreateDTO,
                                           HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            TeamDTO team = teamService.createTeam(teamCreateDTO, currentUserId);
            return ApiResponse.ok(team);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取所有团队列表
     */
    @GetMapping
    public ApiResponse<List<TeamDTO>> getAllTeams() {
        try {
            List<TeamDTO> teams = teamService.getAllTeams();
            return ApiResponse.ok(teams);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 根据ID获取团队
     */
    @GetMapping("/{id}")
    public ApiResponse<TeamDTO> getTeamById(@PathVariable Long id) {
        try {
            TeamDTO team = teamService.getTeamById(id);
            return ApiResponse.ok(team);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 根据编码获取团队
     */
    @GetMapping("/code/{code}")
    public ApiResponse<TeamDTO> getTeamByCode(@PathVariable String code) {
        try {
            TeamDTO team = teamService.getTeamByCode(code);
            return ApiResponse.ok(team);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取用户创建的团队
     */
    @GetMapping("/my/owned")
    public ApiResponse<List<TeamDTO>> getMyOwnedTeams(HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            List<TeamDTO> teams = teamService.getTeamsByOwner(currentUserId);
            return ApiResponse.ok(teams);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取用户参与的团队
     */
    @GetMapping("/my/joined")
    public ApiResponse<List<TeamDTO>> getMyJoinedTeams(HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            List<TeamDTO> teams = teamService.getTeamsByUser(currentUserId);
            return ApiResponse.ok(teams);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 更新团队信息
     */
    @PutMapping("/{id}")
    public ApiResponse<TeamDTO> updateTeam(@PathVariable Long id,
                                           @RequestBody TeamDTO teamDTO,
                                           HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            TeamDTO updatedTeam = teamService.updateTeam(id, teamDTO, currentUserId);
            return ApiResponse.ok(updatedTeam);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 删除团队
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTeam(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            teamService.deleteTeam(id, currentUserId);
            return new ApiResponse<>(0, "团队删除成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取团队成员列表
     */
    @GetMapping("/{id}/members")
    public ApiResponse<List<TeamMemberDTO>> getTeamMembers(@PathVariable Long id) {
        try {
            List<TeamMemberDTO> members = teamService.getTeamMembers(id);
            return ApiResponse.ok(members);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 添加团队成员
     */
    @PostMapping("/{id}/members")
    public ApiResponse<TeamMemberDTO> addMember(@PathVariable Long id,
                                                @RequestParam Long userId,
                                                @RequestParam String role,
                                                HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            TeamMemberDTO member = teamService.addMember(id, userId, role, currentUserId);
            return ApiResponse.ok(member);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 移除团队成员
     */
    @DeleteMapping("/{id}/members/{userId}")
    public ApiResponse<Void> removeMember(@PathVariable Long id,
                                          @PathVariable Long userId,
                                          HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            teamService.removeMember(id, userId, currentUserId);
            return new ApiResponse<>(0, "成员移除成功", null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 更新成员角色
     */
    @PutMapping("/{id}/members/{userId}/role")
    public ApiResponse<TeamMemberDTO> updateMemberRole(@PathVariable Long id,
                                                       @PathVariable Long userId,
                                                       @RequestParam String role,
                                                       HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            TeamMemberDTO member = teamService.updateMemberRole(id, userId, role, currentUserId);
            return ApiResponse.ok(member);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 生成团队邀请链接
     */
    @PostMapping("/{id}/generate-invite-link")
    public ApiResponse<String> generateInviteLink(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "7") int expireDays,
                                                  HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            // 创建公开邀请（不指定被邀请者）
            TeamInvitation invitation = teamInvitationService.createInvitation(
                    id, currentUserId, null, null, "member", "公开邀请链接");

            String inviteLink = "http://localhost:28080/api/v1/teams/invitations/" + invitation.getInvitationCode();
            return ApiResponse.ok(inviteLink);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取团队的公开信息（用于邀请页面）
     */
    @GetMapping("/public/{teamId}")
    public ApiResponse<?> getTeamPublicInfo(@PathVariable Long teamId) {
        try {
            TeamDTO team = teamService.getTeamById(teamId);

            // 只返回公开信息
            var publicInfo = new java.util.HashMap<String, Object>();
            publicInfo.put("id", team.getId());
            publicInfo.put("name", team.getName());
            publicInfo.put("description", team.getDescription());
            publicInfo.put("avatarUrl", team.getAvatarUrl());
            publicInfo.put("visibility", team.getVisibility());
            publicInfo.put("memberCount", team.getMemberCount());

            return ApiResponse.ok(publicInfo);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 邀请用户加入团队（通过邮箱）
     */
    @PostMapping("/{id}/invite-by-email")
    public ApiResponse<Void> inviteByEmail(@PathVariable Long id,
                                           @RequestParam String email,
                                           @RequestParam(defaultValue = "member") String role,
                                           @RequestParam(required = false) String message,
                                           HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            teamInvitationService.createInvitation(id, currentUserId, email, null, role, message);
            return new ApiResponse<>(0, "邀请已发送", null);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 获取团队的邀请列表
     */
    @GetMapping("/{id}/invitations")
    public ApiResponse<List<TeamInvitation>> getTeamInvitations(@PathVariable Long id,
                                                                HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            if (currentUserId == null) {
                return ApiResponse.fail(401, "未授权访问");
            }

            // 验证用户有权限查看邀请列表
            var teamMember = teamService.getTeamMembers(id).stream()
                    .filter(member -> member.getUserId().equals(currentUserId))
                    .findFirst();

            if (teamMember.isEmpty() ||
                    !("owner".equals(teamMember.get().getRole()) || "admin".equals(teamMember.get().getRole()))) {
                return ApiResponse.fail(403, "没有权限查看邀请列表");
            }

            List<TeamInvitation> invitations = teamInvitationService.getTeamInvitations(id);
            return ApiResponse.ok(invitations);
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}