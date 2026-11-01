
package com.socialdashboard.repository;

import com.socialdashboard.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository
        extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :user1 AND m.receiver.id = :user2)
           OR (m.sender.id = :user2 AND m.receiver.id = :user1)
        ORDER BY m.sentAt ASC
        """)
    List<Message> findConversation(
            @Param("user1") Long user1,
            @Param("user2") Long user2
    );

    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    @Query("""
        SELECT m FROM Message m
        WHERE m.sender.id = :userId
           OR m.receiver.id = :userId
        ORDER BY m.sentAt DESC
        """)
    List<Message> findRecentMessages(
            @Param("userId") Long userId
    );
}
