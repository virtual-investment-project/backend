package com.investment.backend.battle.scheduler;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.battle.service.BattleProfitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfitSnapshotScheduler {

    private final BattleRepository battleRepository;
    private final BattleProfitService battleProfitService;

    /**
     * 5분마다 진행 중인(PROGRESS) 배틀의 모든 계좌 수익률 스냅샷 저장
     * - AccountHistory에 PROFIT_SNAPSHOT 타입으로 기록
     * - Team.rate, TeamUser.rate 동시 갱신
     */
    @Scheduled(fixedRate = 300_000) // 5분 = 300,000ms
    public void saveActiveBattleSnapshots() {
        List<Battle> activeBattles = battleRepository.findByStatusOrderByCreatedAtDesc(BattleStatus.PROGRESS);

        if (activeBattles.isEmpty()) {
            return;
        }

        log.info("수익률 스냅샷 스케줄러 실행 - 진행 중인 배틀 수: {}개", activeBattles.size());

        for (Battle battle : activeBattles) {
            try {
                battleProfitService.updateRatesAndSaveSnapshots(battle.getId());
            } catch (Exception e) {
                log.error("배틀 수익률 스냅샷 저장 실패 - battleId: {}, 에러: {}",
                        battle.getId(), e.getMessage());
            }
        }
    }
}
