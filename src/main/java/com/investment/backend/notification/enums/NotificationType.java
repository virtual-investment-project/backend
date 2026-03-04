package com.investment.backend.notification.enums;

/**
 * 알림 유형
 * ORDER_FILLED: 주문 체결
 * BATTLE_START: 팀전 시작
 * RANK_CHANGE: 순위 변동
 * PRICE_ALERT: 관심 종목 가격 알림
 */
public enum NotificationType {
    ORDER_FILLED,
    BATTLE_START,
    RANK_CHANGE,
    PRICE_ALERT
}
