
package com.socialdashboard.service;

import com.socialdashboard.dto.PostRequest;
import com.socialdashboard.model.*;
import com.socialdashboard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PostService {

    private final PostRepository posts;
    private final UserRepository users;
    private final LikeRepository likes;
    private final CommentRepository comments;
    private final NotificationService notificationService;

    public PostService(
            PostRepository posts,
            UserRepository users,
            LikeRepository likes,
            CommentRepository comments,
            NotificationService notificationService) {

        this.posts = posts;
        this.users = users;
        this.likes = likes;
        this.comments = comments;
        this.notificationService = notificationService;
    }

    private AppUser current(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<Map<String, Object>> feed(String email) {
        AppUser viewer = current(email);

        return posts.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(p -> view(p, viewer))
                .toList();
    }

    public Map<String, Object> create(PostRequest request, String email) {
        Post post = new Post();

        post.setCaption(request.getCaption());
        post.setImageUrl(request.getImageUrl());
        post.setUser(current(email));

        return view(posts.save(post), post.getUser());
    }

    @Transactional
    public void delete(Long id, String email) {

        Post post = posts.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (!post.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException(
                    "You can only delete your own posts"
            );
        }

        likes.deleteAll(
                likes.findAll()
                        .stream()
                        .filter(l -> l.getPost().getId().equals(id))
                        .toList()
        );

        comments.deleteCommentsByPostId(id);

        posts.delete(post);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long id, String email) {

        Post post = posts.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        AppUser user = current(email);

        boolean liked = likes.existsByPostIdAndUserId(id, user.getId());

        if (liked) {
            // Unlike: remove the existing like
            likes.deleteByPostIdAndUserId(id, user.getId());

        } else {
            // Like: save the like
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            likes.save(like);

            // Notify the post owner
            notificationService.createNotification(
                    post.getUser(),
                    user,
                    "LIKE",
                    user.getUsername() + " liked your post",
                    post.getId()
            );
        }

        return Map.of(
                "liked", !liked,
                "likesCount", likes.countByPostId(id)
        );
    }

    private Map<String, Object> view(Post p, AppUser viewer) {

        return Map.of(
                "id", p.getId(),
                "caption", p.getCaption(),
                "imageUrl", p.getImageUrl() == null ? "" : p.getImageUrl(),
                "createdAt", p.getCreatedAt().toString(),
                "user", Map.of(
                        "id", p.getUser().getId(),
                        "username", p.getUser().getUsername()
                ),
                "likesCount", likes.countByPostId(p.getId()),
                "likedByMe", likes.existsByPostIdAndUserId(
                        p.getId(), viewer.getId()
                ),
                "ownedByMe", p.getUser().getId().equals(viewer.getId())
        );
    }
}
