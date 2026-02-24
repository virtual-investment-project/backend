package com.investment.backend.battle.scheduler;

import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.notification.enums.NotificationType;
import com.investment.backend.notification.service.NotificationService;
import com.investment.backend.team.entity.Team;
import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.team.repository.TeamRepository;
import com.investment.backend.team.repository.TeamUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankUpdateScheduler {

    private final BattleRepository battleRepository;
    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final NotificationService notificationService;

    // 1분마다 진행 중인 배틀의 팀 순위 업데이트
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateTeamRanks() {
        // PROGRESS 상태 배틀 조회
        List<Battle> activeBattles = battleRepository.findByStatusOrderByCreatedAtDesc(BattleStatus.PROGRESS);

        for (Battle battle : activeBattles) {
            try {
                processRankUpdate(battle);
            } catch (Exception e) {
                log.error("순위 업데이트 실패 - 배틀 ID: {}, 에러: {}", battle.getId(), e.getMessage());
            }
        }
    }

    private void processRankUpdate(Battle battle) {
        List<Team> teams = teamRepository.findByBattleId(battle.getId());

        if (teams.size() < 2) {
            return;
        }

        // 수익률(rate) 기준 내림차순 정렬 → 순위 계산
        List<Team> sortedTeams = teams.stream()
                .sorted(Comparator.comparing(Team::getRate).reversed())
                .toList();

        for (int i = 0; i < sortedTeams.size(); i++) {
            Team team = sortedTeams.get(i);
            int newRank = i + 1;

            team.updateRank(newRank);

            // 순위 변동 감지 → 알림
            if (team.hasRankChanged()) {
                String direction = team.getCurrentRank() < team.getPreviousRank() ? "↑" : "↓";
                String title = String.format("순위 변동 %s", direction);
                String message = String.format("'%s'에서 '%s' 팀이 %d위 → %d위 (수익률: %.1f%%)",
                        battle.getName(), team.getName(),
                        team.getPreviousRank(), team.getCurrentRank(), team.getRate());

                // 해당 팀원들에게 알림
                List<TeamUser> members = teamUserRepository.findByTeamIdAndStatus(
                        team.getId(), TeamUserStatus.ACTIVE);
                for (TeamUser member : members) {
                    notificationService.createNotification(
                            member.getUser(), NotificationType.RANK_CHANGE, title, message, null);
                }

                log.info("순위 변동 알림 - 배틀: {}, 팀: {}, {} → {}",
                        battle.getName(), team.getName(),
                        team.getPreviousRank(), team.getCurrentRank());
            }
        }
    }
}
