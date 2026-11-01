package com.socialdashboard.repository;

import com.socialdashboard.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
