package com.investment.backend.battle.scheduler;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.repository.BattleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleStatusScheduler {

    private final BattleRepository battleRepository;

    /**
     * 1분마다 배틀 상태 자동 전환
     * YET → PROGRESS: startAt 도달
     * PROGRESS → END: endAt 도달
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void updateBattleStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // YET → PROGRESS
        List<Battle> toStart = battleRepository.findYetBattlesToStart(now);
        for (Battle battle : toStart) {
            battle.updateStatus();
            log.info("배틀 시작: {} ({})", battle.getName(), battle.getId());
        }

        // PROGRESS → END
        List<Battle> toEnd = battleRepository.findProgressBattlesToEnd(now);
        for (Battle battle : toEnd) {
            battle.updateStatus();
            log.info("배틀 종료: {} ({})", battle.getName(), battle.getId());
        }
    }
}
