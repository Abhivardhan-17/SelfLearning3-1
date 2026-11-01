
package com.socialdashboard.repository;

import com.socialdashboard.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost_IdOrderByCreatedAtAsc(Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteCommentsByPostId(@Param("postId") Long postId);
}
