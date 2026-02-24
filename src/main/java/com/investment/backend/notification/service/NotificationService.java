package com.investment.backend.notification.service;

import com.investment.backend.notification.dto.NotificationResponse;
import com.investment.backend.notification.entity.Notification;
import com.investment.backend.notification.enums.NotificationType;
import com.investment.backend.notification.repository.NotificationRepository;
import com.investment.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 알림 생성 (유저의 알림 설정을 확인하여, 비활성화된 알림은 생성하지 않음)
    public Notification createNotification(User user, NotificationType type, String title, String message,
            String data) {
        // 유저의 알림 설정 확인
        if (!isNotificationEnabled(user, type)) {
            log.debug("알림 설정 비활성화 - 유저: {}, 알림 유형: {}", user.getId(), type);
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .data(data)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("알림 생성 - ID: {}, 유저: {}, 유형: {}, 제목: {}", saved.getId(), user.getId(), type, title);
        return saved;
    }

    // 알림 목록 조회 (최신 50개)
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(User user) {
        return notificationRepository.findTop50ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    // 안 읽은 알림 수 조회
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    // 개별 알림 읽음 처리
    public void markAsRead(UUID notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
    }

    // 전체 알림 읽음 처리
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsReadByUserId(user.getId());
    }

    // 유저의 알림 설정 확인
    private boolean isNotificationEnabled(User user, NotificationType type) {
        return switch (type) {
            case ORDER_FILLED -> user.getOrderExecution();
            case BATTLE_START -> user.getBattleStart();
            case RANK_CHANGE -> user.getRankChange();
            case PRICE_ALERT -> user.getStockPriceAlert();
        };
    }
}
