package edu.cit.garbo.pawnscan.features.notifications;

import edu.cit.garbo.pawnscan.features.notifications.dto.FcmTokenRequest;
import edu.cit.garbo.pawnscan.features.notifications.dto.NotificationResponse;
import edu.cit.garbo.pawnscan.shared.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtService jwtService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> notifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return notificationService.getNotifications(resolveAuthenticatedName(authentication), page, size);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> unreadCount(Authentication authentication) {
        return Map.of("count", notificationService.getUnreadCount(resolveAuthenticatedName(authentication)));
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public NotificationResponse markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId) {
        return notificationService.markAsRead(resolveAuthenticatedName(authentication), notificationId);
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Boolean> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(resolveAuthenticatedName(authentication));
        return Map.of("success", true);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String, Boolean> clearNotifications(Authentication authentication) {
        notificationService.clearNotifications(resolveAuthenticatedName(authentication));
        return Map.of("success", true);
    }

    @PostMapping("/fcm-tokens")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Boolean> registerFcmToken(
            Authentication authentication,
            @Valid @org.springframework.web.bind.annotation.RequestBody FcmTokenRequest request) {
        notificationService.registerFcmToken(
                resolveAuthenticatedName(authentication),
                request.getToken(),
                request.getPlatform());
        return Map.of("success", true);
    }

    @PostMapping("/fcm-tokens/unregister")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Boolean> unregisterFcmToken(
            Authentication authentication,
            @Valid @org.springframework.web.bind.annotation.RequestBody FcmTokenRequest request) {
        notificationService.unregisterFcmToken(resolveAuthenticatedName(authentication), request.getToken());
        return Map.of("success", true);
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam("token") String token) {
        if (!jwtService.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid notification stream token");
        }

        Claims claims = jwtService.parseClaims(token);
        return notificationService.subscribe(claims.getSubject());
    }

    private String resolveAuthenticatedName(Authentication authentication) {
        Authentication currentAuthentication = authentication != null
                ? authentication
                : SecurityContextHolder.getContext().getAuthentication();

        if (currentAuthentication == null || currentAuthentication.getName() == null) {
            throw new IllegalStateException("Authentication is required");
        }

        return currentAuthentication.getName();
    }
}
