package com.investment.backend.battle.repository;

import com.investment.backend.battle.entity.Battle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BattleRepository extends JpaRepository<Battle, UUID> {

    List<Battle> findAllByOrderByCreatedAtDesc();
}
