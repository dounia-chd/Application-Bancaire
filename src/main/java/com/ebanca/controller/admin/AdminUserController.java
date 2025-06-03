package com.ebanca.controller.admin;

import com.ebanca.model.User;
import com.ebanca.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<User> users = userService.findAll(pageRequest);
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/users/create";
    }

    @PostMapping("/create")
    public String createUser(
            @ModelAttribute User user,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        try {
            user.setPassword(password); // Le service s'occupera du hachage
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success", "User created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "admin/users/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute User user,
            RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setEmail(user.getEmail());
            
            userService.save(existingUser);
            redirectAttributes.addFlashAttribute("success", "User updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/enable")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.enableUser(id);
            redirectAttributes.addFlashAttribute("success", "User enabled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error enabling user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/disable")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.disableUser(id);
            redirectAttributes.addFlashAttribute("success", "User disabled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error disabling user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/lock")
    public String lockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.lockUser(id);
            redirectAttributes.addFlashAttribute("success", "User locked successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error locking user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.unlockUser(id);
            redirectAttributes.addFlashAttribute("success", "User unlocked successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error unlocking user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/search")
    public String searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userService.searchUsers(query, pageRequest);
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("query", query);
        
        return "admin/users/list";
    }
} 