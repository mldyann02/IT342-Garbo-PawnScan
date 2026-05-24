package edu.cit.garbo.pawnscan.features.notifications;

import edu.cit.garbo.pawnscan.features.notifications.dto.NotificationResponse;
import edu.cit.garbo.pawnscan.shared.user.User;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(User recipient, String title, String message);

    NotificationResponse createNotification(User recipient, String title, String message, String targetUrl);

    List<NotificationResponse> getNotifications(String authenticatedEmail, int page, int size);

    long getUnreadCount(String authenticatedEmail);

    NotificationResponse markAsRead(String authenticatedEmail, Long notificationId);

    void markAllAsRead(String authenticatedEmail);

    SseEmitter subscribe(String authenticatedEmail);
}
