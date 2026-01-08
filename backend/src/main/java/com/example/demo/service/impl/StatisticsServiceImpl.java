// src/main/java/com/example/demo/service/impl/StatisticsServiceImpl.java
package com.example.demo.service.impl;

import com.example.demo.app.entity.Team;
import com.example.demo.app.entity.User;
import com.example.demo.app.mapper.TeamMapper;
import com.example.demo.app.mapper.TeamMemberMapper;
import com.example.demo.app.mapper.UserMapper;
import com.example.demo.dto.SystemStatsDTO;
import com.example.demo.dto.TeamStatsDTO;
import com.example.demo.dto.UserStatsDTO;
import com.example.demo.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    // 系统启动时间（用于计算运行时长）
    private final LocalDateTime systemStartTime = LocalDateTime.now();

    @Override
    public UserStatsDTO getUserStatistics() {
        UserStatsDTO stats = new UserStatsDTO();

        try {
            // 获取所有用户
            List<User> allUsers = userMapper.selectAll();

            // 统计总数
            stats.setTotalUsers((long) allUsers.size());

            // 统计活跃用户（状态为1的用户）
            long activeUsers = allUsers.stream()
                    .filter(user -> user.getStatus() != null && user.getStatus() == 1)
                    .count();
            stats.setActiveUsers(activeUsers);

            // 统计今日新用户（简化实现，实际应该根据created_at字段）
            stats.setNewUsersToday(0L); // 简化实现

            // 统计本周新用户（简化实现）
            stats.setNewUsersThisWeek(0L); // 简化实现

            // 按角色统计用户（简化实现）
            Map<String, Long> usersByRole = new HashMap<>();
            usersByRole.put("user", (long) allUsers.size()); // 简化实现
            stats.setUsersByRole(usersByRole);

            // 按状态统计用户
            Map<String, Long> usersByStatus = new HashMap<>();
            usersByStatus.put("active", activeUsers);
            usersByStatus.put("inactive", stats.getTotalUsers() - activeUsers);
            stats.setUsersByStatus(usersByStatus);

        } catch (Exception e) {
            // 统计失败时返回空数据
            System.err.println("用户统计失败: " + e.getMessage());
        }

        return stats;
    }

    @Override
    public TeamStatsDTO getTeamStatistics() {
        TeamStatsDTO stats = new TeamStatsDTO();

        try {
            // 获取所有团队
            List<Team> allTeams = teamMapper.selectAll();

            // 统计总数
            stats.setTotalTeams((long) allTeams.size());

            // 统计活跃团队（状态为1的团队）
            long activeTeams = allTeams.stream()
                    .filter(team -> team.getStatus() != null && team.getStatus() == 1)
                    .count();
            stats.setActiveTeams(activeTeams);

            // 统计今日新团队（简化实现）
            stats.setNewTeamsToday(0L);

            // 统计本周新团队（简化实现）
            stats.setNewTeamsThisWeek(0L);

            // 按可见性统计团队
            Map<String, Long> teamsByVisibility = new HashMap<>();
            teamsByVisibility.put("public",
                    allTeams.stream().filter(team -> "public".equals(team.getVisibility())).count());
            teamsByVisibility.put("private",
                    allTeams.stream().filter(team -> "private".equals(team.getVisibility())).count());
            stats.setTeamsByVisibility(teamsByVisibility);

            // 按状态统计团队
            Map<String, Long> teamsByStatus = new HashMap<>();
            teamsByStatus.put("active", activeTeams);
            teamsByStatus.put("inactive", stats.getTotalTeams() - activeTeams);
            stats.setTeamsByStatus(teamsByStatus);

            // 统计团队成员总数
            long totalMembers = 0L;
            for (Team team : allTeams) {
                int memberCount = teamMemberMapper.countByTeamId(team.getId());
                totalMembers += memberCount;
            }
            stats.setTotalTeamMembers(totalMembers);

            // 计算平均成员数
            if (stats.getTotalTeams() > 0) {
                stats.setAvgMembersPerTeam((double) totalMembers / stats.getTotalTeams());
            }

        } catch (Exception e) {
            // 统计失败时返回空数据
            System.err.println("团队统计失败: " + e.getMessage());
        }

        return stats;
    }

    @Override
    public SystemStatsDTO getSystemStatistics() {
        SystemStatsDTO stats = new SystemStatsDTO();

        try {
            // 用户统计
            List<User> users = userMapper.selectAll();
            stats.setTotalUsers((long) users.size());

            // 团队统计
            List<Team> teams = teamMapper.selectAll();
            stats.setTotalTeams((long) teams.size());

            // 角色和权限统计（简化实现）
            stats.setTotalRoles(5L); // 预设5个角色
            stats.setTotalPermissions(28L); // 预设28个权限

            // 登录统计（简化实现）
            stats.setTodayLogins(0L);
            stats.setWeekLogins(0L);

            // 系统运行时间
            stats.setSystemStartTime(systemStartTime);
            stats.setUptimeHours(java.time.Duration.between(systemStartTime, LocalDateTime.now()).toHours());

        } catch (Exception e) {
            // 统计失败时返回空数据
            System.err.println("系统统计失败: " + e.getMessage());
        }

        return stats;
    }

    @Override
    public Long getTodayActiveUsers() {
        try {
            // 简化实现：返回活跃用户数
            List<User> users = userMapper.selectAll();
            return users.stream()
                    .filter(user -> user.getStatus() != null && user.getStatus() == 1)
                    .count();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public Long getThisWeekNewUsers() {
        try {
            // 简化实现：返回总用户数
            List<User> users = userMapper.selectAll();
            return (long) users.size();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public Map<String, Object> getTeamMemberStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Team> teams = teamMapper.selectAll();

            // 团队数量分布
            stats.put("totalTeams", teams.size());

            // 成员总数
            long totalMembers = 0L;
            for (Team team : teams) {
                int memberCount = teamMemberMapper.countByTeamId(team.getId());
                totalMembers += memberCount;
            }
            stats.put("totalMembers", totalMembers);

            // 平均成员数
            if (teams.size() > 0) {
                stats.put("avgMembersPerTeam", (double) totalMembers / teams.size());
            } else {
                stats.put("avgMembersPerTeam", 0.0);
            }

            // 最大团队成员数
            int maxMembers = 0;
            for (Team team : teams) {
                int memberCount = teamMemberMapper.countByTeamId(team.getId());
                if (memberCount > maxMembers) {
                    maxMembers = memberCount;
                }
            }
            stats.put("maxTeamMembers", maxMembers);

        } catch (Exception e) {
            // 统计失败时返回空数据
            System.err.println("团队成员统计失败: " + e.getMessage());
            stats.put("error", "统计失败");
        }

        return stats;
    }
}
