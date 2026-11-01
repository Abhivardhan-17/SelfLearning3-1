package com.socialdashboard.controller;

import com.socialdashboard.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Map<String, Object>> getNotifications(Principal principal) {
        return notificationService.getNotifications(principal.getName());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Principal principal) {
        return Map.of("count",
                notificationService.getUnreadCount(principal.getName()));
    }

    @PatchMapping("/{id}/read")
    public Map<String, String> markAsRead(
            @PathVariable Long id, Principal principal) {
        notificationService.markAsRead(id, principal.getName());
        return Map.of("message", "Notification marked as read");
    }

    @PatchMapping("/read-all")
    public Map<String, String> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return Map.of("message", "All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteNotification(
            @PathVariable Long id, Principal principal) {
        notificationService.deleteNotification(id, principal.getName());
        return Map.of("message", "Notification deleted");
    }

    @DeleteMapping
    public Map<String, String> clearNotifications(Principal principal) {
        notificationService.clearNotifications(principal.getName());
        return Map.of("message", "All notifications deleted");
    }
}