package com.investment.backend.battle.repository;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BattleRepository extends JpaRepository<Battle, UUID> {

    List<Battle> findAllByOrderByCreatedAtDesc();

    // 특정 상태의 배틀 조회
    List<Battle> findByStatusOrderByCreatedAtDesc(BattleStatus status);

    /**
     * YET → PROGRESS 전환 대상: startAt이 지났고 아직 YET인 배틀
     */
    @Query("SELECT b FROM Battle b WHERE b.status = 'YET' AND b.startAt <= :now")
    List<Battle> findYetBattlesToStart(@Param("now") LocalDateTime now);

    /**
     * PROGRESS → END 전환 대상: endAt이 지났고 아직 PROGRESS인 배틀
     */
    @Query("SELECT b FROM Battle b WHERE b.status = 'PROGRESS' AND b.endAt <= :now")
    List<Battle> findProgressBattlesToEnd(@Param("now") LocalDateTime now);
}
