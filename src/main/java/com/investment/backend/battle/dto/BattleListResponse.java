package com.investment.backend.battle.dto;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.enums.BattleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Battle 목록 조회용 응답 DTO
 * 각 팀의 수익률 요약 정보 포함
 */
@Getter
@Builder
public class BattleListResponse {

    private UUID id;
    private BattleType type;
    private String name;
    private String ticker;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BattleStatus status;
    private LocalDateTime createdAt;
    private List<TeamSummary> teams;

    @Getter
    @Builder
    public static class TeamSummary {
        private Long id;
        private String name;
        private Float rate;
        private Integer proceed;
        private Integer memberCount;
    }

    public static BattleListResponse from(Battle battle, List<TeamSummary> teams) {
        return BattleListResponse.builder()
                .id(battle.getId())
                .type(battle.getType())
                .name(battle.getName())
                .ticker(battle.getTicker())
                .startAt(battle.getStartAt())
                .endAt(battle.getEndAt())
                .status(battle.getStatus())
                .createdAt(battle.getCreatedAt())
                .teams(teams)
                .build();
    }
}
