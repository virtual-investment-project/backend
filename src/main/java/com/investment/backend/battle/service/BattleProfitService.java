package com.investment.backend.battle.service;

import com.investment.backend.account.dto.AccountProfitResponse;
import com.investment.backend.account.entity.Account;
import com.investment.backend.account.repository.AccountRepository;
import com.investment.backend.battle.dto.TeamProfitResponse;
import com.investment.backend.history.service.AccountHistoryService;
import com.investment.backend.team.entity.Team;
import com.investment.backend.team.entity.TeamUser;
import com.investment.backend.team.enums.TeamUserStatus;
import com.investment.backend.team.repository.TeamRepository;
import com.investment.backend.team.repository.TeamUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BattleProfitService {

    private final AccountRepository accountRepository;
    private final TeamRepository teamRepository;
    private final TeamUserRepository teamUserRepository;
    private final AccountHistoryService accountHistoryService;

    /**
     * 배틀 내 모든 계좌의 개인별 수익률 조회 (수익률 내림차순 랭킹)
     */
    public List<AccountProfitResponse> getAccountProfits(UUID battleId) {
        List<Account> accounts = accountRepository.findByBattleId(battleId);

        List<AccountProfitResponse> result = accounts.stream()
                .map(AccountProfitResponse::from)
                .sorted(Comparator.comparingDouble(AccountProfitResponse::getReturnRate).reversed())
                .toList();

        return result;
    }

    /**
     * 배틀 내 팀별 합산 수익률 조회 (수익률 내림차순 랭킹)
     * - ACTIVE 팀원의 계좌만 합산 (LEFT/KICKED 제외)
     * - 각 팀 내 멤버 개인 수익률도 함께 반환
     */
    public List<TeamProfitResponse> getTeamProfits(UUID battleId) {
        List<Team> teams = teamRepository.findByBattleId(battleId);

        List<TeamProfitResponse> teamProfits = new ArrayList<>();

        for (Team team : teams) {
            // ACTIVE 팀원 ID만 필터링
            Set<UUID> activeMemberUserIds = teamUserRepository
                    .findByTeamIdAndStatus(team.getId(), TeamUserStatus.ACTIVE)
                    .stream()
                    .map(tu -> tu.getUser().getId())
                    .collect(Collectors.toSet());

            List<Account> activeAccounts = accountRepository.findByTeamId(team.getId())
                    .stream()
                    .filter(a -> activeMemberUserIds.contains(a.getUser().getId()))
                    .toList();

            long totalSeedMoney = activeAccounts.stream()
                    .mapToLong(Account::getSeedMoney)
                    .sum();
            long totalAsset = activeAccounts.stream()
                    .mapToLong(Account::getTotalAsset)
                    .sum();
            long returnAmount = totalAsset - totalSeedMoney;
            double returnRate = TeamProfitResponse.calculateTeamReturnRate(totalAsset, totalSeedMoney);

            List<AccountProfitResponse> members = activeAccounts.stream()
                    .map(AccountProfitResponse::from)
                    .sorted(Comparator.comparingDouble(AccountProfitResponse::getReturnRate).reversed())
                    .toList();

            teamProfits.add(TeamProfitResponse.builder()
                    .teamId(team.getId())
                    .teamName(team.getName())
                    .battleId(battleId)
                    .totalSeedMoney(totalSeedMoney)
                    .totalAsset(totalAsset)
                    .returnAmount(returnAmount)
                    .returnRate(returnRate)
                    .memberCount(members.size())
                    .rank(0) // 정렬 후 부여
                    .members(members)
                    .build());
        }

        // 수익률 내림차순 정렬 후 rank 부여
        teamProfits.sort(Comparator.comparingDouble(TeamProfitResponse::getReturnRate).reversed());
        AtomicInteger rank = new AtomicInteger(1);
        return teamProfits.stream()
                .map(tp -> TeamProfitResponse.builder()
                        .teamId(tp.getTeamId())
                        .teamName(tp.getTeamName())
                        .battleId(tp.getBattleId())
                        .totalSeedMoney(tp.getTotalSeedMoney())
                        .totalAsset(tp.getTotalAsset())
                        .returnAmount(tp.getReturnAmount())
                        .returnRate(tp.getReturnRate())
                        .memberCount(tp.getMemberCount())
                        .rank(rank.getAndIncrement())
                        .members(tp.getMembers())
                        .build())
                .toList();
    }

    /**
     * 배틀 내 모든 계좌 수익률 스냅샷 저장 및 Team/TeamUser rate 업데이트
     * - ProfitSnapshotScheduler에서 주기적으로 호출
     */
    @Transactional
    public void updateRatesAndSaveSnapshots(UUID battleId) {
        List<Account> accounts = accountRepository.findByBattleId(battleId);
        if (accounts.isEmpty()) {
            return;
        }

        // 1. 계좌별 스냅샷 저장 + TeamUser.rate 업데이트
        for (Account account : accounts) {
            try {
                accountHistoryService.recordProfitSnapshot(account);
            } catch (Exception e) {
                log.error("수익률 스냅샷 저장 실패 - 계좌 ID: {}, 에러: {}", account.getId(), e.getMessage());
            }

            if (account.getTeam() != null) {
                double returnRate = AccountProfitResponse.calculateReturnRate(
                        account.getTotalAsset(), account.getSeedMoney());

                Optional<TeamUser> teamUserOpt = teamUserRepository
                        .findByTeamIdAndUserIdAndStatus(
                                account.getTeam().getId(),
                                account.getUser().getId(),
                                TeamUserStatus.ACTIVE);
                teamUserOpt.ifPresent(tu -> tu.updateRate((float) returnRate));
            }
        }

        // 2. Team.rate 업데이트 (ACTIVE 팀원만 합산)
        List<Team> teams = teamRepository.findByBattleId(battleId);
        for (Team team : teams) {
            Set<UUID> activeMemberUserIds = teamUserRepository
                    .findByTeamIdAndStatus(team.getId(), TeamUserStatus.ACTIVE)
                    .stream()
                    .map(tu -> tu.getUser().getId())
                    .collect(Collectors.toSet());

            List<Account> activeAccounts = accountRepository.findByTeamId(team.getId())
                    .stream()
                    .filter(a -> activeMemberUserIds.contains(a.getUser().getId()))
                    .toList();

            if (activeAccounts.isEmpty()) continue;

            long totalAsset = activeAccounts.stream().mapToLong(Account::getTotalAsset).sum();
            long totalSeedMoney = activeAccounts.stream().mapToLong(Account::getSeedMoney).sum();
            double teamRate = TeamProfitResponse.calculateTeamReturnRate(totalAsset, totalSeedMoney);
            team.updateRate((float) teamRate);
        }

        log.info("배틀 수익률 업데이트 완료 - battleId: {}, 계좌 수: {}", battleId, accounts.size());
    }
}
