package com.socialdashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2000) private String caption;
    private String imageUrl;
    private LocalDateTime createdAt = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public Long getId() { return id; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
