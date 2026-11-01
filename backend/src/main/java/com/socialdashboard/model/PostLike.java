package com.socialdashboard.model;

import jakarta.persistence.*;

@Entity
@Table(name = "post_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class PostLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "post_id", nullable = false) private Post post;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) private AppUser user;
    public Long getId() { return id; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
