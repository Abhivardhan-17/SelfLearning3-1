
package com.socialdashboard.service;

import com.socialdashboard.model.AppUser;
import com.socialdashboard.model.Message;
import com.socialdashboard.repository.MessageRepository;
import com.socialdashboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final MessageRepository messages;
    private final UserRepository users;

    public MessageService(
            MessageRepository messages,
            UserRepository users) {
        this.messages = messages;
        this.users = users;
    }

    private AppUser getUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public Map<String, Object> sendMessage(
            String senderEmail,
            Long receiverId,
            String content) {

        AppUser sender = getUser(senderEmail);

        AppUser receiver = users.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("You cannot message yourself");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        if (content.length() > 2000) {
            throw new RuntimeException("Message is too long");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content.trim());

        return toMap(messages.save(message));
    }

    @Transactional
    public List<Map<String, Object>> getConversation(
            String email,
            Long otherUserId) {

        AppUser currentUser = getUser(email);

        users.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> conversation = messages.findConversation(
                currentUser.getId(), otherUserId);

        conversation.stream()
                .filter(message ->
                        message.getReceiver().getId()
                                .equals(currentUser.getId()))
                .forEach(message -> message.setRead(true));

        return conversation.stream()
                .map(this::toMap)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentConversations(String email) {

        AppUser currentUser = getUser(email);
        Long currentUserId = currentUser.getId();

        List<Message> allMessages =
                messages.findRecentMessages(currentUserId);

        Map<Long, Map<String, Object>> conversations =
                new LinkedHashMap<>();

        for (Message message : allMessages) {

            AppUser otherUser = message.getSender().getId()
                    .equals(currentUserId)
                    ? message.getReceiver()
                    : message.getSender();

            Long otherUserId = otherUser.getId();

            Map<String, Object> conversation =
                    conversations.get(otherUserId);

            if (conversation == null) {
                conversation = new LinkedHashMap<>();

                conversation.put("userId", otherUserId);
                conversation.put("username", otherUser.getUsername());
                conversation.put("lastMessage", message.getContent());
                conversation.put("lastMessageAt",
                        message.getSentAt().toString());
                conversation.put("lastMessageSenderId",
                        message.getSender().getId());
                conversation.put("unreadCount", 0L);

                conversations.put(otherUserId, conversation);
            }

            if (message.getReceiver().getId().equals(currentUserId)
                    && !message.isRead()) {

                long unreadCount =
                        (long) conversation.get("unreadCount");

                conversation.put("unreadCount", unreadCount + 1);
            }
        }

        return new ArrayList<>(conversations.values());
    }

    // Unsend a message sent by the logged-in user
    @Transactional
    public Map<String, Object> unsendMessage(
            String email,
            Long messageId) {

        AppUser currentUser = getUser(email);

        Message message = messages.findById(messageId)
                .orElseThrow(() ->
                        new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "You can only unsend your own messages");
        }

        messages.delete(message);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Message unsent successfully");

        return result;
    }

    private Map<String, Object> toMap(Message message) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("id", message.getId());
        result.put("senderId", message.getSender().getId());
        result.put("senderUsername", message.getSender().getUsername());
        result.put("receiverId", message.getReceiver().getId());
        result.put("receiverUsername", message.getReceiver().getUsername());
        result.put("content", message.getContent());
        result.put("sentAt", message.getSentAt().toString());
        result.put("read", message.isRead());

        return result;
    }
}
