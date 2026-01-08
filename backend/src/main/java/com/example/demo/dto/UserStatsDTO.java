// src/main/java/com/example/demo/dto/UserStatsDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class UserStatsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Map<String, Long> usersByRole;
    private Map<String, Long> usersByStatus;

    public UserStatsDTO() {
        this.totalUsers = 0L;
        this.activeUsers = 0L;
        this.newUsersToday = 0L;
        this.newUsersThisWeek = 0L;
    }
}