
package com.socialdashboard.controller;

import com.socialdashboard.repository.*;
import com.socialdashboard.service.FollowService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;
    private final PostRepository posts;
    private final FollowService followService;

    public UserController(
            UserRepository users,
            PostRepository posts,
            FollowService followService) {
        this.users = users;
        this.posts = posts;
        this.followService = followService;
    }

    // Get logged-in user's profile
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {

        var user = users.findByEmail(auth.getName())
                .orElseThrow();

        return Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "bio", user.getBio() == null ? "" : user.getBio(),
                "postsCount", posts.countByUserId(user.getId())
        );
    }

    // Search users by username
    @GetMapping("/search")
    public List<Map<String, Object>> searchUsers(
            @RequestParam(defaultValue = "") String q,
            Authentication auth) {

        String searchText = q.trim().toLowerCase();

        Long currentUserId = users.findByEmail(auth.getName())
                .orElseThrow()
                .getId();

        if (searchText.isEmpty()) {
            return List.of();
        }

        return users.findAll()
                .stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .filter(user -> user.getUsername()
                        .toLowerCase()
                        .contains(searchText))
                .limit(20)
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "bio", user.getBio() == null ? "" : user.getBio(),
                        "postsCount", posts.countByUserId(user.getId())
                ))
                .toList();
    }

    // Get another user's public profile and posts
    @GetMapping("/{id}")
    public Map<String, Object> getPublicProfile(
            @PathVariable Long id,
            Authentication auth) {

        var user = users.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Map<String, Object>> userPosts =
                posts.findByUserIdOrderByCreatedAtDesc(id)
                        .stream()
                        .map(post -> {
                            Map<String, Object> postData =
                                    new LinkedHashMap<>();

                            postData.put("id", post.getId());
                            postData.put("caption", post.getCaption());
                            postData.put("imageUrl", post.getImageUrl());
                            postData.put("createdAt",
                                    post.getCreatedAt().toString());

                            return postData;
                        })
                        .toList();

        Map<String, Object> profile = new LinkedHashMap<>();

        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("bio", user.getBio() == null ? "" : user.getBio());
        profile.put("postsCount", userPosts.size());
        profile.put("posts", userPosts);

        profile.put("followersCount",
                followService.getFollowersCount(id));

        profile.put("followingCount",
                followService.getFollowingCount(id));

        profile.put("isFollowing",
                followService.isFollowing(auth.getName(), id));

        return profile;
    }
}
