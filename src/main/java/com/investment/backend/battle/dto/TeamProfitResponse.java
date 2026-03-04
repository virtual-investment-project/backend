package com.investment.backend.battle.dto;

import com.investment.backend.account.dto.AccountProfitResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class TeamProfitResponse {

    private Long teamId;
    private String teamName;
    private UUID battleId;
    private Long totalSeedMoney;  // 팀원 전체 시드머니 합
    private Long totalAsset;      // 팀원 전체 총 자산 합
    private Long returnAmount;    // 합산 수익금 = totalAsset - totalSeedMoney
    private Double returnRate;    // 합산 수익률(%) - 소수점 2자리
    private int memberCount;
    private int rank;
    private List<AccountProfitResponse> members; // 팀원 개인별 수익률 (수익률 내림차순)

    /**
     * 팀 합산 수익률 계산 (소수점 2자리 반올림)
     */
    public static double calculateTeamReturnRate(Long totalAsset, Long totalSeedMoney) {
        if (totalSeedMoney == null || totalSeedMoney == 0) {
            return 0.0;
        }
        double raw = ((totalAsset - totalSeedMoney) / (double) totalSeedMoney) * 100.0;
        return BigDecimal.valueOf(raw)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
