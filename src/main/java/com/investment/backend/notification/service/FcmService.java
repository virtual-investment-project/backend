package com.investment.backend.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.investment.backend.notification.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FcmService {

    private static final String ANDROID_CHANNEL_ID = "investment_notifications";

    public void sendPushNotification(String fcmToken, NotificationType type, String title, String body, String dataJson) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("FCM 토큰 없음 - 푸시 알림 생략 (type={})", type);
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.debug("Firebase 미초기화 - 푸시 알림 생략 (type={})", type);
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    // Android 8.0+ 알림 에 필수: 체널 ID 지정
                    .setAndroidConfig(AndroidConfig.builder()
                            .setNotification(AndroidNotification.builder()
                                    .setChannelId(ANDROID_CHANNEL_ID)
                                    .build())
                            .build())
                    .putData("type", type.name());

            if (dataJson != null && !dataJson.isBlank()) {
                messageBuilder.putData("data", dataJson);
            }

            String messageId = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("FCM 푸시 전송 완료 - messageId: {}, type: {}", messageId, type);

        } catch (FirebaseMessagingException e) {
            log.warn("FCM 푸시 전송 실패 - type: {}, code: {}, message: {}", type, e.getMessagingErrorCode(), e.getMessage());
        }
    }
}
