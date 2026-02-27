package com.investment.backend.team.repository;

import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.enums.TeamUserRole;
import com.investment.backend.team.enums.TeamUserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamUserRepository extends JpaRepository<TeamUser, Long> {

    long countByTeamIdAndStatus(Long teamId, TeamUserStatus status);

    List<TeamUser> findByTeamIdAndStatus(Long teamId, TeamUserStatus status);

    Optional<TeamUser> findByTeamIdAndUserIdAndStatus(Long teamId, UUID userId, TeamUserStatus status);

    boolean existsByTeamIdAndUserIdAndStatus(Long teamId, UUID userId, TeamUserStatus status);

    // 특정 Battle에서 해당 유저가 이미 팀에 가입되어 있는지 확인
    boolean existsByTeam_Battle_IdAndUserIdAndStatus(UUID battleId, UUID userId, TeamUserStatus status);

    // LEADER가 아닌 활성 팀원 조회 (리더 이전용)
    List<TeamUser> findByTeamIdAndStatusAndRoleNot(Long teamId, TeamUserStatus status, TeamUserRole role);

    // 특정 배틀에서 해당 유저가 LEADER인지 확인
    boolean existsByTeam_Battle_IdAndUserIdAndStatusAndRole(
            UUID battleId, UUID userId, TeamUserStatus status, TeamUserRole role);
}
