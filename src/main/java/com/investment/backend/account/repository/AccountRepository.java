package com.investment.backend.account.repository;

import com.investment.backend.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * 유저의 개인 계좌 조회 (team_id, battle_id = NULL)
     */
    Optional<Account> findByUserIdAndTeamIsNullAndBattleIsNull(UUID userId);

    /**
     * 유저의 특정 배틀 계좌 조회
     */
    Optional<Account> findByUserIdAndBattleId(UUID userId, UUID battleId);

    /**
     * 유저의 모든 계좌 조회
     */
    List<Account> findByUserId(UUID userId);

    /**
     * 특정 팀의 모든 계좌 조회
     */
    List<Account> findByTeamId(Long teamId);

    /**
     * 특정 배틀의 모든 계좌 조회
     */
    List<Account> findByBattleId(UUID battleId);
}
