
package com.socialdashboard.service;

import com.socialdashboard.model.AppUser;
import com.socialdashboard.model.Follow;
import com.socialdashboard.repository.FollowRepository;
import com.socialdashboard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowService {

    private final FollowRepository follows;
    private final UserRepository users;
    private final NotificationService notifications;

    public FollowService(
            FollowRepository follows,
            UserRepository users,
            NotificationService notifications) {
        this.follows = follows;
        this.users = users;
        this.notifications = notifications;
    }

    private AppUser getUser(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void followUser(String email, Long targetUserId) {

        AppUser follower = getUser(email);

        AppUser following = users.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (follower.getId().equals(following.getId())) {
            throw new RuntimeException("You cannot follow yourself");
        }

        if (follows.existsByFollowerIdAndFollowingId(
                follower.getId(), following.getId())) {
            return;
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        follows.save(follow);

        notifications.createNotification(
                following,
                follower,
                "FOLLOW",
                follower.getUsername() + " started following you",
                null
        );
    }

    @Transactional
    public void unfollowUser(String email, Long targetUserId) {

        AppUser follower = getUser(email);

        follows.findByFollowerIdAndFollowingId(
                follower.getId(), targetUserId
        ).ifPresent(follows::delete);
    }

    public boolean isFollowing(String email, Long targetUserId) {

        AppUser follower = getUser(email);

        return follows.existsByFollowerIdAndFollowingId(
                follower.getId(), targetUserId
        );
    }

    public long getFollowersCount(Long userId) {
        return follows.countByFollowingId(userId);
    }

    public long getFollowingCount(Long userId) {
        return follows.countByFollowerId(userId);
    }
}
