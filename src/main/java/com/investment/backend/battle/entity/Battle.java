package com.investment.backend.battle.entity;

import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.enums.BattleType;
import com.investment.backend.battle.enums.MetricType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "battle")
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BattleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;

    @Column(nullable = false)
    private LocalTime valuationTime;

    @Column(nullable = false)
    private Integer initialCapital;

    @Column(nullable = false)
    private Integer memberCount;

    @Column(nullable = false)
    private Integer teamCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Battle(BattleType type, String name, String ticker,
            LocalDateTime startAt, LocalDateTime endAt,
            MetricType metricType, LocalTime valuationTime,
            Integer initialCapital, Integer memberCount, Integer teamCount) {
        this.type = type != null ? type : BattleType.NORMAL;
        this.name = name != null ? name : "battle";
        this.ticker = ticker != null ? ticker : "BTC";
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = BattleStatus.YET;
        this.metricType = metricType != null ? metricType : MetricType.RATE;
        this.valuationTime = valuationTime != null ? valuationTime : LocalTime.of(0, 0, 0);
        this.initialCapital = initialCapital;
        this.memberCount = memberCount != null ? memberCount : 10;
        this.teamCount = teamCount != null ? teamCount : 2;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 거래 가능 여부 확인
     * - startAt과 endAt이 같으면 24시간 거래 가능
     * - startAt 이후 && endAt 이전이면 거래 가능
     */
    public boolean isTradingAllowed() {
        LocalDateTime now = LocalDateTime.now();
        
        // startAt과 endAt이 같으면 24시간 거래 가능
        if (startAt.equals(endAt)) {
            return true;
        }
        
        // startAt 이후이고 endAt 이전이면 거래 가능
        return !now.isBefore(startAt) && now.isBefore(endAt);
    }

    /**
     * 현재 시각 기준으로 status 자동 전환
     * YET → PROGRESS: startAt 도달
     * PROGRESS → END: endAt 도달
     */
    public boolean updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        BattleStatus prev = this.status;

        if (this.status == BattleStatus.YET && !now.isBefore(this.startAt)) {
            this.status = BattleStatus.PROGRESS;
        } else if (this.status == BattleStatus.PROGRESS && !now.isBefore(this.endAt)) {
            this.status = BattleStatus.END;
        }

        return this.status != prev; // 변경 여부 반환
    }
}
