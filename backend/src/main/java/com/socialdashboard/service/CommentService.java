
package com.socialdashboard.service;

import com.socialdashboard.model.*;
import com.socialdashboard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CommentService {

    private final CommentRepository comments;
    private final PostRepository posts;
    private final UserRepository users;
    private final NotificationService notificationService;

    public CommentService(CommentRepository comments,
                          PostRepository posts,
                          UserRepository users,
                          NotificationService notificationService) {
        this.comments = comments;
        this.posts = posts;
        this.users = users;
        this.notificationService = notificationService;
    }

    @Transactional
    public Comment addComment(Long postId, String content, String email) {

        Post post = posts.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        AppUser user = users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Comment cannot be empty");
        }

        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = comments.save(comment);

        // Notify the post owner
        notificationService.createNotification(
                post.getUser(),
                user,
                "COMMENT",
                user.getUsername() + " commented on your post",
                post.getId()
        );

        return savedComment;
    }

    public List<Comment> getComments(Long postId) {
        return comments.findByPost_IdOrderByCreatedAtAsc(postId);
    }
}
