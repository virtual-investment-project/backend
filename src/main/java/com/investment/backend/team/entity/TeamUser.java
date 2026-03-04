package com.investment.backend.team.entity;

import com.investment.backend.team.enums.TeamUserRole;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_user")
public class TeamUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamUserRole role;

    @Column(name = "`rank`", nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Float rate; // 개인 수익률

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamUserStatus status;

    @Builder
    public TeamUser(Team team, User user, TeamUserRole role) {
        this.team = team;
        this.user = user;
        this.joinedAt = LocalDateTime.now();
        this.role = role != null ? role : TeamUserRole.MEMBER;
        this.rank = 0;
        this.rate = 0.0f;
        this.updatedAt = LocalDateTime.now();
        this.status = TeamUserStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void leave() {
        this.status = TeamUserStatus.LEFT;
    }

    public void kick() {
        this.status = TeamUserStatus.KICKED;
    }

    public void promoteToLeader() {
        this.role = TeamUserRole.LEADER;
    }

    /**
     * 개인 수익률 업데이트 (소수점 2자리 반올림)
     */
    public void updateRate(float rate) {
        this.rate = Math.round(rate * 100.0f) / 100.0f;
        this.updatedAt = LocalDateTime.now();
    }
}
