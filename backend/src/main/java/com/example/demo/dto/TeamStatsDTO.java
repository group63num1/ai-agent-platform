// src/main/java/com/example/demo/dto/TeamStatsDTO.java
package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TeamStatsDTO {
    private Long totalTeams;
    private Long activeTeams;
    private Long newTeamsToday;
    private Long newTeamsThisWeek;
    private Map<String, Long> teamsByVisibility;
    private Map<String, Long> teamsByStatus;
    private Long totalTeamMembers;
    private Double avgMembersPerTeam;

    public TeamStatsDTO() {
        this.totalTeams = 0L;
        this.activeTeams = 0L;
        this.newTeamsToday = 0L;
        this.newTeamsThisWeek = 0L;
        this.totalTeamMembers = 0L;
        this.avgMembersPerTeam = 0.0;
    }
}
