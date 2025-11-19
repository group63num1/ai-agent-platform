// src/main/java/com/example/demo/dto/SystemStatsDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemStatsDTO {
    private Long totalUsers;
    private Long totalTeams;
    private Long totalRoles;
    private Long totalPermissions;
    private Long todayLogins;
    private Long weekLogins;
    private LocalDateTime systemStartTime;
    private Long uptimeHours;

    public SystemStatsDTO() {
        this.totalUsers = 0L;
        this.totalTeams = 0L;
        this.totalRoles = 0L;
        this.totalPermissions = 0L;
        this.todayLogins = 0L;
        this.weekLogins = 0L;
    }
}