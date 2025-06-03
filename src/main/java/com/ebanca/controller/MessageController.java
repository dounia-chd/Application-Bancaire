package com.ebanca.controller;

import com.ebanca.model.Message;
import com.ebanca.model.User;
import com.ebanca.service.MessageService;
import com.ebanca.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Message>> getReceivedMessages(
            @RequestParam Long userId,
            Pageable pageable) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(messageService.findByReceiver(user, pageable));
    }

    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Message>> getSentMessages(
            @RequestParam Long userId,
            Pageable pageable) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(messageService.findBySender(user, pageable));
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Message>> getUnreadMessages(@RequestParam Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(messageService.findByReceiverAndReadFalse(user));
    }

    @GetMapping("/unread/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadMessagesCount(@RequestParam Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(messageService.countUnreadMessages(user));
    }

    @GetMapping("/priority")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<Message>> getMessagesByPriority(
            @RequestParam Long userId,
            @RequestParam Message.MessagePriority priority,
            Pageable pageable) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(messageService.findByReceiverAndPriority(user, priority, pageable));
    }

    @GetMapping("/conversation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Message>> getConversation(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        User user1 = userService.findById(user1Id)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userService.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));
        return ResponseEntity.ok(messageService.findBySenderAndReceiver(user1, user2));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Message> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam Message.MessagePriority priority) {
        User sender = userService.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        Message message = messageService.sendMessage(sender, receiver, subject, content, priority);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@RequestParam Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        messageService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/conversation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteConversation(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {
        User user1 = userService.findById(user1Id)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userService.findById(user2Id)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));
        messageService.deleteConversation(user1, user2);
        return ResponseEntity.ok().build();
    }
} 