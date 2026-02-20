package com.investment.backend.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettingsUpdateRequest {
    // 테마 설정
    private Boolean darkMode;

    // 알림 설정
    private Boolean orderExecution;
    private Boolean battleStart;
    private Boolean rankChange;
    private Boolean profitRate;
    private Boolean pushNotification;
    private Boolean dailySummary;
    private Boolean stockPriceAlert;
}
