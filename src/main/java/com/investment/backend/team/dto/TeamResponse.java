package com.investment.backend.team.dto;

import com.investment.backend.team.entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class TeamResponse {

    private Long id;
    private UUID battleId;
    private String name;
    private UUID inviteCode;
    private String description;
    private Float rate;
    private Integer proceed;
    private Integer memberCount; // 현재 팀원 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TeamResponse from(Team team, int memberCount) {
        return from(team, memberCount, false);
    }

    public static TeamResponse from(Team team, int memberCount, boolean showInviteCode) {
        return TeamResponse.builder()
                .id(team.getId())
                .battleId(team.getBattle().getId())
                .name(team.getName())
                .inviteCode(showInviteCode ? team.getInviteCode() : null)
                .description(team.getDescription())
                .rate(team.getRate())
                .proceed(team.getProceed())
                .memberCount(memberCount)
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }
}
