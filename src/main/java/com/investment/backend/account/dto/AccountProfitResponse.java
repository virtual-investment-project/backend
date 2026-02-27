package com.investment.backend.account.dto;

import com.investment.backend.account.entity.Account;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
@Builder
public class AccountProfitResponse {

    private UUID accountId;
    private UUID userId;
    private String userName;
    private Long teamId;      // 배틀 계좌인 경우 팀 ID (개인 계좌는 null)
    private String teamName;  // 배틀 계좌인 경우 팀명 (개인 계좌는 null)
    private Long seedMoney;   // 초기 투자금
    private Long totalAsset;  // 현재 총 자산
    private Long returnAmount; // 수익금 = totalAsset - seedMoney
    private Double returnRate; // 수익률(%) - 소수점 2자리

    public static AccountProfitResponse from(Account account) {
        long returnAmount = account.getTotalAsset() - account.getSeedMoney();
        double returnRate = calculateReturnRate(account.getTotalAsset(), account.getSeedMoney());

        return AccountProfitResponse.builder()
                .accountId(account.getId())
                .userId(account.getUser().getId())
                .userName(account.getUser().getName())
                .teamId(account.getTeam() != null ? account.getTeam().getId() : null)
                .teamName(account.getTeam() != null ? account.getTeam().getName() : null)
                .seedMoney(account.getSeedMoney())
                .totalAsset(account.getTotalAsset())
                .returnAmount(returnAmount)
                .returnRate(returnRate)
                .build();
    }

    /**
     * 수익률 계산 (소수점 2자리 반올림)
     */
    public static double calculateReturnRate(Long totalAsset, Long seedMoney) {
        if (seedMoney == null || seedMoney == 0) {
            return 0.0;
        }
        double raw = ((totalAsset - seedMoney) / (double) seedMoney) * 100.0;
        return BigDecimal.valueOf(raw)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
