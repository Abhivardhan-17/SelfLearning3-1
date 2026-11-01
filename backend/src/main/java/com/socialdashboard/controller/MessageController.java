
package com.socialdashboard.controller;

import com.socialdashboard.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:5173")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public Map<String, Object> sendMessage(
            @RequestBody SendMessageRequest request,
            Principal principal) {

        return messageService.sendMessage(
                principal.getName(),
                request.receiverId(),
                request.content()
        );
    }

    @GetMapping("/conversation/{userId}")
    public List<Map<String, Object>> getConversation(
            @PathVariable Long userId,
            Principal principal) {

        return messageService.getConversation(
                principal.getName(),
                userId
        );
    }

    @GetMapping("/recent")
    public List<Map<String, Object>> getRecentConversations(
            Principal principal) {

        return messageService.getRecentConversations(
                principal.getName()
        );
    }

    // Unsend a message
    @DeleteMapping("/{messageId}")
    public Map<String, Object> unsendMessage(
            @PathVariable Long messageId,
            Principal principal) {

        return messageService.unsendMessage(
                principal.getName(),
                messageId
        );
    }

    public record SendMessageRequest(
            Long receiverId,
            String content
    ) {}
}
