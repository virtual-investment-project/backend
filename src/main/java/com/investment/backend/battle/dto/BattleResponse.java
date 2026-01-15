package com.investment.backend.battle.dto;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.enums.BattleType;
import com.investment.backend.battle.enums.MetricType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
public class BattleResponse {

    private UUID id;
    private BattleType type;
    private String name;
    private String ticker;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BattleStatus status;
    private MetricType metricType;
    private LocalTime valuationTime;
    private Integer initialCapital;
    private Integer memberCount;
    private Integer teamCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BattleResponse from(Battle battle) {
        return BattleResponse.builder()
                .id(battle.getId())
                .type(battle.getType())
                .name(battle.getName())
                .ticker(battle.getTicker())
                .startAt(battle.getStartAt())
                .endAt(battle.getEndAt())
                .status(battle.getStatus())
                .metricType(battle.getMetricType())
                .valuationTime(battle.getValuationTime())
                .initialCapital(battle.getInitialCapital())
                .memberCount(battle.getMemberCount())
                .teamCount(battle.getTeamCount())
                .createdAt(battle.getCreatedAt())
                .updatedAt(battle.getUpdatedAt())
                .build();
    }
}
