package com.ebanca.repository;

import com.ebanca.model.Message;
import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByReceiver(User receiver, Pageable pageable);
    Page<Message> findBySender(User sender, Pageable pageable);
    List<Message> findByReceiverAndReadFalse(User receiver);
    Page<Message> findByReceiverAndPriority(User receiver, Message.MessagePriority priority, Pageable pageable);
    List<Message> findBySenderAndReceiver(User sender, User receiver);
    long countByReceiverAndReadFalse(User receiver);
    
    // Nouvelles méthodes pour l'administration
    Page<Message> findByPriority(Message.MessagePriority priority, Pageable pageable);
    Page<Message> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Message> findBySubjectContainingOrContentContaining(String subject, String content, Pageable pageable);
    Page<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByCreatedAtDesc(
        User sender1, User receiver1, User sender2, User receiver2, Pageable pageable);
    List<Message> findByCreatedAtBetweenAndPriorityAndReceiver(
        LocalDateTime startDate, LocalDateTime endDate, Message.MessagePriority priority, User receiver);
    List<Message> findByCreatedAtBetweenAndReceiver(LocalDateTime startDate, LocalDateTime endDate, User receiver);
    List<Message> findByCreatedAtBetweenAndPriority(
        LocalDateTime startDate, LocalDateTime endDate, Message.MessagePriority priority);
    List<Message> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
} 