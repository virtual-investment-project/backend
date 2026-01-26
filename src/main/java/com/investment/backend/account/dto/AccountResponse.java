package com.investment.backend.account.dto;

import com.investment.backend.account.entity.Account;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AccountResponse {
    private UUID id;
    private String name;
    private Long balance;
    private Long seedMoney;
    private Long totalAsset;

    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .balance(account.getBalance())
                .seedMoney(account.getSeedMoney())
                .totalAsset(account.getTotalAsset())
                .build();
    }
}
