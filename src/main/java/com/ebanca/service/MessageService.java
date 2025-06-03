package com.ebanca.service;

import com.ebanca.model.Message;
import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageService extends GenericService<Message, Long> {
    Page<Message> findByReceiver(User receiver, Pageable pageable);
    Page<Message> findBySender(User sender, Pageable pageable);
    List<Message> findByReceiverAndReadFalse(User receiver);
    Page<Message> findByReceiverAndPriority(User receiver, Message.MessagePriority priority, Pageable pageable);
    List<Message> findBySenderAndReceiver(User sender, User receiver);
    long countUnreadMessages(User receiver);
    Message sendMessage(User sender, User receiver, String subject, String content, Message.MessagePriority priority);
    void markAsRead(Long messageId);
    void markAllAsRead(User receiver);
    void deleteMessage(Long messageId);
    void deleteConversation(User user1, User user2);
    
    // Nouvelles méthodes pour l'administration
    Page<Message> findByPriority(Message.MessagePriority priority, Pageable pageable);
    Page<Message> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Message> searchMessages(String query, Pageable pageable);
    Page<Message> findConversation(User user1, User user2, Pageable pageable);
    List<Message> exportMessages(LocalDateTime startDate, LocalDateTime endDate, Message.MessagePriority priority, Long userId);
} 