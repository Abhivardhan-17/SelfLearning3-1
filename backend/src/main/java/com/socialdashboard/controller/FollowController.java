package com.socialdashboard.controller;

import com.socialdashboard.service.FollowService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{userId}")
    public Map<String, String> follow(
            @PathVariable Long userId,
            Principal principal) {

        followService.followUser(principal.getName(), userId);

        return Map.of("message", "User followed successfully");
    }

    @DeleteMapping("/{userId}")
    public Map<String, String> unfollow(
            @PathVariable Long userId,
            Principal principal) {

        followService.unfollowUser(principal.getName(), userId);

        return Map.of("message", "User unfollowed successfully");
    }

    @GetMapping("/{userId}/status")
    public Map<String, Object> getFollowStatus(
            @PathVariable Long userId,
            Principal principal) {

        return Map.of(
                "following",
                followService.isFollowing(principal.getName(), userId),
                "followersCount",
                followService.getFollowersCount(userId),
                "followingCount",
                followService.getFollowingCount(userId)
        );
    }
}