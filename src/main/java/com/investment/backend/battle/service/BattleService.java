package com.investment.backend.battle.service;

import com.investment.backend.battle.dto.BattleListResponse;
import com.investment.backend.battle.dto.BattleResponse;
import com.investment.backend.battle.dto.CreateBattleRequest;
import com.investment.backend.battle.entity.Battle;
import com.investment.backend.battle.repository.BattleRepository;
import com.investment.backend.team.entity.Team;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.team.repository.TeamRepository;
import com.investment.backend.team.repository.TeamUserRepository;
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

    public BattleResponse createBattle(CreateBattleRequest request) {
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
                .map(battle -> {
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
                })
                .toList();
    }
}
