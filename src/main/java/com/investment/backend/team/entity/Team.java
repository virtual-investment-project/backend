package com.investment.backend.team.entity;

import com.investment.backend.battle.entity.Battle;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id", nullable = false)
    private Battle battle;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID inviteCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Float rate;

    @Column(nullable = false)
    private Integer proceed;

    @Column(nullable = false)
    private Integer currentRank = 0;

    @Column(nullable = false)
    private Integer previousRank = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Team(Battle battle, String name, String description) {
        this.battle = battle;
        this.name = name != null ? name : "team";
        this.inviteCode = UUID.randomUUID();
        this.description = description;
        this.rate = 0.0f;
        this.proceed = 0;
        this.currentRank = 0;
        this.previousRank = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void regenerateInviteCode() {
        this.inviteCode = UUID.randomUUID();
    }

    // 순위 업데이트 (이전 순위 보존)
    public void updateRank(Integer newRank) {
        this.previousRank = this.currentRank;
        this.currentRank = newRank;
    }

    // 순위 변동 여부 확인
    public boolean hasRankChanged() {
        return previousRank != 0 && !currentRank.equals(previousRank);
    }
}
