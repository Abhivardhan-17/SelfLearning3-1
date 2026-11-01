package com.socialdashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "follows",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"follower_id", "following_id"}
        )
    }
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private AppUser follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private AppUser following;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Follow() {}

    public Long getId() {
        return id;
    }

    public AppUser getFollower() {
        return follower;
    }

    public void setFollower(AppUser follower) {
        this.follower = follower;
    }

    public AppUser getFollowing() {
        return following;
    }

    public void setFollowing(AppUser following) {
        this.following = following;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}