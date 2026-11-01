package com.socialdashboard.service;

import com.socialdashboard.model.AppUser;
import com.socialdashboard.model.Notification;
import com.socialdashboard.repository.NotificationRepository;
import com.socialdashboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notifications;
    private final UserRepository users;

    public NotificationService(NotificationRepository notifications,
                                UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    private AppUser getUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void createNotification(AppUser recipient, AppUser actor,
                                   String type, String message, Long postId) {

        if (recipient.getId().equals(actor.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setType(type);
        notification.setMessage(message);
        notification.setPostId(postId);

        notifications.save(notification);
    }

    public List<Map<String, Object>> getNotifications(String email) {
        AppUser user = getUser(email);

        return notifications
                .findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toMap)
                .toList();
    }

    public long getUnreadCount(String email) {
        AppUser user = getUser(email);
        return notifications.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long id, String email) {
        AppUser user = getUser(email);

        Notification notification = notifications
                .findByIdAndRecipientId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(String email) {
        AppUser user = getUser(email);

        notifications.findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .forEach(notification -> notification.setRead(true));
    }

    @Transactional
    public void deleteNotification(Long id, String email) {
        AppUser user = getUser(email);
        notifications.deleteByIdAndRecipientId(id, user.getId());
    }

    @Transactional
    public void clearNotifications(String email) {
        AppUser user = getUser(email);
        notifications.deleteByRecipientId(user.getId());
    }

    private Map<String, Object> toMap(Notification notification) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", notification.getId());
        result.put("type", notification.getType());
        result.put("message", notification.getMessage());
        result.put("postId", notification.getPostId());
        result.put("actorUsername", notification.getActor().getUsername());
        result.put("createdAt", notification.getCreatedAt().toString());
        result.put("read", notification.isRead());

        return result;
    }
}