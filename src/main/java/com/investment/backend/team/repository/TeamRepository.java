package com.investment.backend.team.repository;

import com.investment.backend.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByBattleId(UUID battleId);

    long countByBattleId(UUID battleId);

    Optional<Team> findByInviteCode(UUID inviteCode);
}
