package com.investment.backend.team.dto;

import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.enums.TeamUserRole;
import com.investment.backend.team.enums.TeamUserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 팀원 정보 응답 DTO (개인 수익률 포함)
 */
@Getter
@Builder
public class TeamMemberResponse {

    private Long id;
    private UUID userId;
    private String userNickname;
    private TeamUserRole role;
    private Integer rank;
    private Float rate;
    private TeamUserStatus status;
    private LocalDateTime joinedAt;

    public static TeamMemberResponse from(TeamUser teamUser) {
        return TeamMemberResponse.builder()
                .id(teamUser.getId())
                .userId(teamUser.getUser().getId())
                .userNickname(teamUser.getUser().getNickname())
                .role(teamUser.getRole())
                .rank(teamUser.getRank())
                .rate(teamUser.getRate())
                .status(teamUser.getStatus())
                .joinedAt(teamUser.getJoinedAt())
                .build();
    }
}
