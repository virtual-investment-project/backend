package com.investment.backend.account.entity;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.team.entity.Team;
import com.investment.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account")
public class Account {

    private static final Long DEFAULT_PERSONAL_SEED_MONEY = 100000L;
    private static final String DEFAULT_ACCOUNT_NAME = "New Account";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "battle_id")
    private Battle battle;

    @Column(nullable = false)
    private Long balance;

    @Column(nullable = false)
    private Long seedMoney;

    @Column(nullable = false)
    private Long totalAsset;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String name;

    @Builder
    private Account(User user, Team team, Battle battle, Long seedMoney, String name) {
        this.user = user;
        this.team = team;
        this.battle = battle;
        this.seedMoney = seedMoney;
        this.balance = seedMoney;
        this.totalAsset = seedMoney;
        this.createdAt = LocalDateTime.now();
        this.name = name != null ? name : DEFAULT_ACCOUNT_NAME;
    }

    /**
     * 개인 계좌 생성
     * - team_id, battle_id = NULL
     * - 기본 시드머니: $100,000
     */
    public static Account createPersonalAccount(User user) {
        return Account.builder()
                .user(user)
                .team(null)
                .battle(null)
                .seedMoney(DEFAULT_PERSONAL_SEED_MONEY)
                .name("Personal Account")
                .build();
    }

    /**
     * 배틀 계좌 생성
     * - 팀 가입 시 생성
     * - 시드머니는 Battle.initialCapital 사용
     */
    public static Account createBattleAccount(User user, Team team, Battle battle, Long seedMoney) {
        return Account.builder()
                .user(user)
                .team(team)
                .battle(battle)
                .seedMoney(seedMoney)
                .name(battle.getName() + " Account")
                .build();
    }

    /**
     * 예수금 업데이트
     */
    public void updateBalance(Long newBalance) {
        this.balance = newBalance;
    }

    /**
     * 총 자산 업데이트 (현금 + 보유 주식 평가액)
     */
    public void updateTotalAsset(Long newTotalAsset) {
        this.totalAsset = newTotalAsset;
    }

    /**
     * 거래 가능 여부 확인
     * - 개인 계좌는 항상 거래 가능
     * - 배틀 계좌는 배틀의 거래 가능 시간에만 거래 가능
     */
    public boolean isTradingAllowed() {
        // 개인 계좌는 항상 거래 가능
        if (battle == null) {
            return true;
        }
        
        // 배틀 계좌는 배틀의 거래 가능 시간 확인
        return battle.isTradingAllowed();
    }

    /**
     * 거래 가능 여부를 검증하고, 불가능하면 예외를 발생시킴
     */
    public void validateTradingAllowed() {
        if (!isTradingAllowed()) {
            if (battle != null) {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(battle.getStartAt())) {
                    throw new IllegalStateException(
                        "배틀이 아직 시작되지 않았습니다. 시작 시간: " + battle.getStartAt()
                    );
                } else {
                    throw new IllegalStateException(
                        "배틀이 종료되었습니다. 종료 시간: " + battle.getEndAt()
                    );
                }
            }
            throw new IllegalStateException("거래가 불가능합니다.");
        }
    }
}
