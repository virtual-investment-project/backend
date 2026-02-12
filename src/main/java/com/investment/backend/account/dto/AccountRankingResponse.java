package com.investment.backend.account.dto;

import com.investment.backend.account.entity.Account;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AccountRankingResponse {
    private UUID accountId;
    private UUID userId;
    private String accountName;
    private String userName;
    private Long seedMoney;
    private Long totalAsset;
    private Double returnRate; // 수익률 (%)

    public static AccountRankingResponse from(Account account) {
        double returnRate = calculateReturnRate(account.getTotalAsset(), account.getSeedMoney());
        return AccountRankingResponse.builder()
                .accountId(account.getId())
                .userId(account.getUser().getId())
                .accountName(account.getName())
                .userName(account.getUser().getName())
                .seedMoney(account.getSeedMoney())
                .totalAsset(account.getTotalAsset())
                .returnRate(returnRate)
                .build();
    }

    private static double calculateReturnRate(Long totalAsset, Long seedMoney) {
        if (seedMoney == 0) {
            return 0.0;
        }
        return ((totalAsset - seedMoney) / (double) seedMoney) * 100;
    }
}
