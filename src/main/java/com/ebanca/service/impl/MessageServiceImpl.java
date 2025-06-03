package com.ebanca.service.impl;

import com.ebanca.model.Message;
import com.ebanca.model.User;
import com.ebanca.repository.MessageRepository;
import com.ebanca.repository.UserRepository;
import com.ebanca.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MessageServiceImpl extends GenericServiceImpl<Message, Long> implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected MessageRepository getRepository() {
        return messageRepository;
    }

    @Override
    public Page<Message> findByReceiver(User receiver, Pageable pageable) {
        return messageRepository.findByReceiver(receiver, pageable);
    }

    @Override
    public Page<Message> findBySender(User sender, Pageable pageable) {
        return messageRepository.findBySender(sender, pageable);
    }

    @Override
    public List<Message> findByReceiverAndReadFalse(User receiver) {
        return messageRepository.findByReceiverAndReadFalse(receiver);
    }

    @Override
    public Page<Message> findByReceiverAndPriority(User receiver, Message.MessagePriority priority, Pageable pageable) {
        return messageRepository.findByReceiverAndPriority(receiver, priority, pageable);
    }

    @Override
    public List<Message> findBySenderAndReceiver(User sender, User receiver) {
        return messageRepository.findBySenderAndReceiver(sender, receiver);
    }

    @Override
    public long countUnreadMessages(User receiver) {
        return messageRepository.countByReceiverAndReadFalse(receiver);
    }

    @Override
    public Message sendMessage(User sender, User receiver, String subject, String content, Message.MessagePriority priority) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject(subject);
        message.setContent(content);
        message.setPriority(priority);
        message.setRead(0);
        return save(message);
    }

    @Override
    public void markAsRead(Long messageId) {
        Message message = findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));
        
        if (message.getRead() == 0) {
            message.setRead(1);
            save(message);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(User receiver) {
        List<Message> unreadMessages = findByReceiverAndReadFalse(receiver);
        for (Message message : unreadMessages) {
            message.setRead(1);
            save(message);
        }
    }

    @Override
    public void deleteMessage(Long messageId) {
        if (!existsById(messageId)) {
            throw new RuntimeException("Message not found");
        }
        deleteById(messageId);
    }

    @Override
    @Transactional
    public void deleteConversation(User user1, User user2) {
        List<Message> messages = messageRepository.findBySenderAndReceiver(user1, user2);
        messages.addAll(messageRepository.findBySenderAndReceiver(user2, user1));
        
        for (Message message : messages) {
            deleteById(message.getId());
        }
    }

    @Override
    public Page<Message> findByPriority(Message.MessagePriority priority, Pageable pageable) {
        return messageRepository.findByPriority(priority, pageable);
    }

    @Override
    public Page<Message> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return messageRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<Message> searchMessages(String query, Pageable pageable) {
        return messageRepository.findBySubjectContainingOrContentContaining(query, query, pageable);
    }

    @Override
    public Page<Message> findConversation(User user1, User user2, Pageable pageable) {
        return messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreatedAtDesc(
            user1, user2, user2, user1, pageable);
    }

    @Override
    public List<Message> exportMessages(LocalDateTime startDate, LocalDateTime endDate,
            Message.MessagePriority priority, Long userId) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            if (priority != null) {
                return messageRepository.findByCreatedAtBetweenAndPriorityAndReceiver(
                    startDate, endDate, priority, user);
            }
            return messageRepository.findByCreatedAtBetweenAndReceiver(startDate, endDate, user);
        } else if (priority != null) {
            return messageRepository.findByCreatedAtBetweenAndPriority(startDate, endDate, priority);
        }
        return messageRepository.findByCreatedAtBetween(startDate, endDate);
    }
} 