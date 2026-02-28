package com.investment.backend.battle.scheduler;

import com.investment.backend.battle.entity.Battle;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BattleScheduler {

    private final BattleRepository battleRepository;
    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final NotificationService notificationService;

    // 1분마다 배틀 시작/종료 확인
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkBattleStatus() {
        LocalDateTime now = LocalDateTime.now();

        // YET → PROGRESS (시작 시간 도달)
        List<Battle> battlesToStart = battleRepository.findYetBattlesToStart(now);
        for (Battle battle : battlesToStart) {
            try {
                battle.updateStatus();
                log.info("배틀 시작 - ID: {}, 이름: {}", battle.getId(), battle.getName());

                // 참여 중인 모든 유저에게 알림
                notifyBattleParticipants(battle, "팀전 시작",
                        String.format("'%s' 대결이 시작되었습니다!", battle.getName()));
            } catch (Exception e) {
                log.error("배틀 시작 처리 실패 - ID: {}, 에러: {}", battle.getId(), e.getMessage());
            }
        }

        // PROGRESS → END (종료 시간 도달)
        List<Battle> battlesToEnd = battleRepository.findProgressBattlesToEnd(now);
        for (Battle battle : battlesToEnd) {
            try {
                battle.updateStatus();
                log.info("배틀 종료 - ID: {}, 이름: {}", battle.getId(), battle.getName());

                notifyBattleParticipants(battle, "팀전 종료",
                        String.format("'%s' 대결이 종료되었습니다!", battle.getName()));
            } catch (Exception e) {
                log.error("배틀 종료 처리 실패 - ID: {}, 에러: {}", battle.getId(), e.getMessage());
            }
        }
    }

    // 배틀 참여자 전원에게 알림 발송
    private void notifyBattleParticipants(Battle battle, String title, String message) {
        List<Team> teams = teamRepository.findByBattleId(battle.getId());
        for (Team team : teams) {
            List<TeamUser> members = teamUserRepository.findByTeamIdAndStatus(team.getId(), TeamUserStatus.ACTIVE);
            for (TeamUser member : members) {
                notificationService.createNotification(
                        member.getUser(), NotificationType.BATTLE_START, title, message, null);
            }
        }
    }
}
