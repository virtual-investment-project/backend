package com.investment.backend.battle.dto;

import com.investment.backend.battle.enums.BattleType;
import com.investment.backend.battle.enums.MetricType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class CreateBattleRequest {

    private BattleType type;

    private String name;

    private String ticker;

    @NotNull(message = "대결 시작일은 필수입니다.")
    private LocalDateTime startAt;

    @NotNull(message = "대결 종료일은 필수입니다.")
    private LocalDateTime endAt;

    private MetricType metricType;

    private LocalTime valuationTime;

    @NotNull(message = "초기 금액은 필수입니다.")
    @Min(value = 1, message = "초기 금액은 1 이상이어야 합니다.")
    private Integer initialCapital;

    private Integer memberCount;

    private Integer teamCount; // 팀 수 제한 (기본값: 2)

    private String teamName; // 생성자의 팀 이름 (선택, 미입력시 "OOO의 팀")
}
