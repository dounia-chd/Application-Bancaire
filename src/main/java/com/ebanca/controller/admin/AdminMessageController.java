package com.ebanca.controller.admin;

import com.ebanca.model.Message;
import com.ebanca.model.User;
import com.ebanca.service.MessageService;
import com.ebanca.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessageController {

    private final MessageService messageService;
    private final UserService userService;

    public AdminMessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping
    public String listMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Message> messages = messageService.findByReceiver(userService.getCurrentUser(), pageRequest);
        
        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalItems", messages.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        
        return "admin/messages/list";
    }

    @GetMapping("/{id}")
    public String viewMessage(@PathVariable Long id, Model model) {
        Message message = messageService.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        model.addAttribute("message", message);
        return "admin/messages/view";
    }

    @GetMapping("/user/{userId}")
    public String getUserMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageService.findByReceiver(user, pageRequest);
        
        model.addAttribute("messages", messages);
        model.addAttribute("user", user);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalItems", messages.getTotalElements());
        
        return "admin/messages/user-messages";
    }

    @GetMapping("/priority/{priority}")
    public String getMessagesByPriority(
            @PathVariable Message.MessagePriority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageService.findByPriority(priority, pageRequest);
        
        model.addAttribute("messages", messages);
        model.addAttribute("priority", priority);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalItems", messages.getTotalElements());
        
        return "admin/messages/priority-messages";
    }

    @GetMapping("/date-range")
    public String getMessagesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageService.findByDateRange(startDate, endDate, pageRequest);
        
        model.addAttribute("messages", messages);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalItems", messages.getTotalElements());
        
        return "admin/messages/date-range-messages";
    }

    @GetMapping("/search")
    public String searchMessages(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageService.searchMessages(query, pageRequest);
        
        model.addAttribute("messages", messages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalItems", messages.getTotalElements());
        model.addAttribute("query", query);
        
        return "admin/messages/list";
    }

    @GetMapping("/compose")
    public String showComposeForm(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("priorities", Message.MessagePriority.values());
        return "admin/messages/compose";
    }

    @PostMapping("/send")
    public String sendMessage(
            @RequestParam Long receiverId,
            @RequestParam String subject,
            @RequestParam String content,
            @RequestParam Message.MessagePriority priority,
            RedirectAttributes redirectAttributes) {
        try {
            User receiver = userService.findById(receiverId)
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
            User sender = userService.getCurrentUser();
            Message message = messageService.sendMessage(sender, receiver, subject, content, priority);
            redirectAttributes.addFlashAttribute("success", "Message sent successfully");
            return "redirect:/admin/messages/" + message.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sending message: " + e.getMessage());
            return "redirect:/admin/messages/compose";
        }
    }

    @PostMapping("/{id}/mark-read")
    public String markAsRead(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            messageService.markAsRead(id);
            redirectAttributes.addFlashAttribute("success", "Message marked as read");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error marking message as read: " + e.getMessage());
        }
        return "redirect:/admin/messages/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteMessage(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            messageService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Message deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting message: " + e.getMessage());
        }
        return "redirect:/admin/messages";
    }

    @GetMapping("/conversation/{userId}")
    public String viewConversation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User currentUser = userService.getCurrentUser();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> messages = messageService.findConversation(currentUser, user, pageRequest);
        
        model.addAttribute("messages", messages);
        model.addAttribute("user", user);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalItems", messages.getTotalElements());
        
        return "admin/messages/conversation";
    }

    @GetMapping("/export")
    public String exportMessages(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Message.MessagePriority priority,
            @RequestParam(required = false) Long userId,
            Model model) {
        
        List<Message> messages = messageService.exportMessages(startDate, endDate, priority, userId);
        model.addAttribute("messages", messages);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("priority", priority);
        model.addAttribute("userId", userId);
        
        return "admin/messages/export";
    }
} 