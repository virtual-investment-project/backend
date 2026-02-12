package com.investment.backend.battle.repository;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BattleRepository extends JpaRepository<Battle, UUID> {

    List<Battle> findAllByOrderByCreatedAtDesc();

    /**
     * 특정 상태의 배틀 조회
     */
    List<Battle> findByStatusOrderByCreatedAtDesc(BattleStatus status);
}
