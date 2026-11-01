
package com.socialdashboard.repository;

import com.socialdashboard.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    long countByUserId(Long userId);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}
