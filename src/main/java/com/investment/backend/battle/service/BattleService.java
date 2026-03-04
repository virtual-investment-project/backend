package com.investment.backend.battle.service;

import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.battle.dto.BattleListResponse;
import com.investment.backend.battle.dto.BattleResponse;
import com.investment.backend.battle.dto.CreateBattleRequest;
import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.enums.BattleStatus;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.team.entity.Team;
import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.enums.TeamUserRole;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.team.repository.TeamRepository;
import com.investment.backend.team.repository.TeamUserRepository;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BattleService {

    private final BattleRepository battleRepository;
    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final AccountRepository accountRepository;

    /**
     * 배틀 생성
     * - 배틀 생성 시 자동으로 첫 번째 팀 생성
     * - 생성자가 해당 팀의 LEADER로 자동 등록
     * - 배틀 계좌 자동 생성
     */
    public BattleResponse createBattle(CreateBattleRequest request, User user) {
        Battle battle = Battle.builder()
                .type(request.getType())
                .name(request.getName())
                .ticker(request.getTicker())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .metricType(request.getMetricType())
                .valuationTime(request.getValuationTime())
                .initialCapital(request.getInitialCapital())
                .memberCount(request.getMemberCount())
                .teamCount(request.getTeamCount())
                .build();

        Battle savedBattle = battleRepository.save(battle);

        // 첫 번째 팀 자동 생성
        Team team = Team.builder()
                .battle(savedBattle)
                .name(request.getTeamName() != null ? request.getTeamName() : user.getName() + "의 팀")
                .build();
        Team savedTeam = teamRepository.save(team);

        // 생성자를 LEADER로 등록
        TeamUser teamUser = TeamUser.builder()
                .team(savedTeam)
                .user(user)
                .role(TeamUserRole.LEADER)
                .build();
        teamUserRepository.save(teamUser);

        // 배틀 계좌 자동 생성
        Account battleAccount = Account.createBattleAccount(
                user, savedTeam, savedBattle, (long) savedBattle.getInitialCapital());
        accountRepository.save(battleAccount);

        return BattleResponse.from(savedBattle);
    }

    @Transactional(readOnly = true)
    public BattleResponse getBattle(UUID battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new IllegalArgumentException("Battle을 찾을 수 없습니다."));
        return BattleResponse.from(battle);
    }

    /**
     * Battle 목록 조회 (팀 수익률 요약 포함)
     */
    @Transactional(readOnly = true)
    public List<BattleListResponse> getAllBattles() {
        List<Battle> battles = battleRepository.findAllByOrderByCreatedAtDesc();

        return battles.stream()
                .map(this::convertToBattleListResponse)
                .toList();
    }

    /**
     * 특정 상태의 배틀 조회 (limit 적용)
     */
    @Transactional(readOnly = true)
    public List<BattleListResponse> getBattlesByStatus(BattleStatus status, int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit은 1 이상 100 이하의 값이어야 합니다.");
        }
        
        return battleRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .limit(limit)
                .map(this::convertToBattleListResponse)
                .toList();
    }

    /**
     * Battle을 BattleListResponse로 변환
     */
    private BattleListResponse convertToBattleListResponse(Battle battle) {
        List<Team> teams = teamRepository.findByBattleId(battle.getId());
        List<BattleListResponse.TeamSummary> teamSummaries = teams.stream()
                .map(team -> {
                    int memberCount = (int) teamUserRepository.countByTeamIdAndStatus(
                            team.getId(), TeamUserStatus.ACTIVE);
                    return BattleListResponse.TeamSummary.builder()
                            .id(team.getId())
                            .name(team.getName())
                            .rate(team.getRate())
                            .proceed(team.getProceed())
                            .memberCount(memberCount)
                            .build();
                })
                .toList();
        return BattleListResponse.from(battle, teamSummaries);
    }
}
