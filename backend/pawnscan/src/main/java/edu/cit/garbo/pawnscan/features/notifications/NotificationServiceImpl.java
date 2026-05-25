package edu.cit.garbo.pawnscan.features.notifications;

import edu.cit.garbo.pawnscan.features.notifications.dto.NotificationResponse;
import edu.cit.garbo.pawnscan.features.notifications.entity.Notification;
import edu.cit.garbo.pawnscan.features.notifications.repository.NotificationRepository;
import edu.cit.garbo.pawnscan.shared.exception.ForbiddenActionException;
import edu.cit.garbo.pawnscan.shared.user.User;
import edu.cit.garbo.pawnscan.shared.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final long SSE_TIMEOUT_MS = 30L * 60L * 1000L;
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public NotificationResponse createNotification(User recipient, String title, String message) {
        return createNotification(recipient, title, message, null);
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(User recipient, String title, String message, String targetUrl) {
        if (recipient == null || recipient.getUserId() == null) {
            throw new IllegalArgumentException("Notification recipient is required");
        }

        Notification saved = notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .targetUrl(targetUrl)
                .build());

        NotificationResponse response = toResponse(saved);
        pushToRecipient(recipient.getUserId(), response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String authenticatedEmail, int page, int size) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(user.getUserId(), pageable)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        return notificationRepository.countByRecipientUserIdAndReadFalse(user.getUserId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(String authenticatedEmail, Long notificationId) {
        User user = getAuthenticatedUser(authenticatedEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!Objects.equals(notification.getRecipient().getUserId(), user.getUserId())) {
            throw new ForbiddenActionException("You can only update your own notifications");
        }

        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientUserIdAndReadFalse(user.getUserId());

        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional
    public void clearNotifications(String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        notificationRepository.deleteByRecipientUserId(user.getUserId());
    }

    @Override
    public SseEmitter subscribe(String authenticatedEmail) {
        User user = getAuthenticatedUser(authenticatedEmail);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emittersByUser.computeIfAbsent(user.getUserId(), ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(user.getUserId(), emitter));
        emitter.onTimeout(() -> removeEmitter(user.getUserId(), emitter));
        emitter.onError(ignored -> removeEmitter(user.getUserId(), emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("unreadCount", notificationRepository.countByRecipientUserIdAndReadFalse(user.getUserId()))));
        } catch (IOException ex) {
            removeEmitter(user.getUserId(), emitter);
        }

        return emitter;
    }

    private User getAuthenticatedUser(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new ForbiddenActionException("Unauthorized access");
        }

        return userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ForbiddenActionException("Authenticated user was not found"));
    }

    private void pushToRecipient(Long recipientId, NotificationResponse notification) {
        List<SseEmitter> emitters = emittersByUser.get(recipientId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException | IllegalStateException ex) {
                removeEmitter(recipientId, emitter);
                LOGGER.debug("Removed closed notification stream for user {}", recipientId, ex);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByUser.remove(userId);
        }
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .notifId(notification.getNotifId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .targetUrl(notification.getTargetUrl())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
