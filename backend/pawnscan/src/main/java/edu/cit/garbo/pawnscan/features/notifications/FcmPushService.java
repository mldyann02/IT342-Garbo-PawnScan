package edu.cit.garbo.pawnscan.features.notifications;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import edu.cit.garbo.pawnscan.features.notifications.dto.NotificationResponse;
import edu.cit.garbo.pawnscan.features.notifications.entity.FcmDeviceToken;
import edu.cit.garbo.pawnscan.features.notifications.repository.FcmDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmPushService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmPushService.class);

    private final FcmDeviceTokenRepository fcmDeviceTokenRepository;

    @Transactional
    public void sendToUserDevices(Long userId, NotificationResponse notification) {
        if (FirebaseApp.getApps().isEmpty()) {
            LOGGER.debug("Skipping FCM push because Firebase Admin SDK is not initialized.");
            return;
        }

        List<FcmDeviceToken> deviceTokens = fcmDeviceTokenRepository.findByUserUserId(userId);
        if (deviceTokens.isEmpty()) {
            return;
        }

        for (FcmDeviceToken deviceToken : deviceTokens) {
            try {
                FirebaseMessaging.getInstance().send(buildMessage(deviceToken.getToken(), notification));
            } catch (FirebaseMessagingException ex) {
                if (isInvalidToken(ex)) {
                    fcmDeviceTokenRepository.deleteByToken(deviceToken.getToken());
                    LOGGER.info("Removed invalid FCM token for user {}", userId);
                } else {
                    LOGGER.warn("Failed to send FCM notification to user {}: {}", userId, ex.getMessage());
                }
            }
        }
    }

    private Message buildMessage(String token, NotificationResponse notification) {
        String title = valueOrDefault(notification.getTitle(), "PawnScan Notification");
        String body = valueOrDefault(notification.getMessage(), "You have a new PawnScan notification.");

        return Message.builder()
                .setToken(token)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(dataPayload(notification, title, body))
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setChannelId("pawnscan_alerts")
                                .build())
                        .build())
                .build();
    }

    private Map<String, String> dataPayload(NotificationResponse notification, String title, String body) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("title", title);
        data.put("message", body);
        data.put("notifId", String.valueOf(notification.getNotifId()));
        if (notification.getTargetUrl() != null) {
            data.put("targetUrl", notification.getTargetUrl());
        }
        if (notification.getCreatedAt() != null) {
            data.put("createdAt", notification.getCreatedAt().toString());
        }
        return data;
    }

    private boolean isInvalidToken(FirebaseMessagingException ex) {
        return ex.getMessagingErrorCode() != null
                && (ex.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || ex.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT);
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
