package com.ebanca.controller.admin;

import com.ebanca.model.Account;
import com.ebanca.model.User;
import com.ebanca.service.AccountService;
import com.ebanca.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final AccountService accountService;
    private final UserService userService;

    public AdminAccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public String listAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Account> accounts = accountService.findAll(pageRequest);
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accounts.getTotalPages());
        model.addAttribute("totalItems", accounts.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        
        return "admin/accounts/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("accountTypes", Account.AccountType.values());
        return "admin/accounts/create";
    }

    @PostMapping("/create")
    public String createAccount(
            @RequestParam Long userId,
            @RequestParam Account.AccountType type,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            accountService.createAccount(user, type);
            redirectAttributes.addFlashAttribute("success", "Account created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating account: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/{id}")
    public String viewAccount(@PathVariable Long id, Model model) {
        Account account = accountService.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        model.addAttribute("account", account);
        return "admin/accounts/view";
    }

    @PostMapping("/{id}/deposit")
    public String deposit(
            @PathVariable Long id,
            @RequestParam double amount,
            RedirectAttributes redirectAttributes) {
        try {
            accountService.deposit(id, BigDecimal.valueOf(amount));
            redirectAttributes.addFlashAttribute("success", "Deposit successful");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing deposit: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    @PostMapping("/{id}/withdraw")
    public String withdraw(
            @PathVariable Long id,
            @RequestParam double amount,
            RedirectAttributes redirectAttributes) {
        try {
            accountService.withdraw(id, BigDecimal.valueOf(amount));
            redirectAttributes.addFlashAttribute("success", "Withdrawal successful");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing withdrawal: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestParam Long sourceAccountId,
            @RequestParam Long targetAccountId,
            @RequestParam double amount,
            RedirectAttributes redirectAttributes) {
        try {
            accountService.transfer(sourceAccountId, targetAccountId, BigDecimal.valueOf(amount));
            redirectAttributes.addFlashAttribute("success", "Transfer successful");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing transfer: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @PostMapping("/{id}/freeze")
    public String freezeAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.freezeAccount(id);
            redirectAttributes.addFlashAttribute("success", "Account frozen successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error freezing account: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    @PostMapping("/{id}/unfreeze")
    public String unfreezeAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.unfreezeAccount(id);
            redirectAttributes.addFlashAttribute("success", "Account unfrozen successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error unfreezing account: " + e.getMessage());
        }
        return "redirect:/admin/accounts/" + id;
    }

    @PostMapping("/{id}/close")
    public String closeAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            accountService.closeAccount(id);
            redirectAttributes.addFlashAttribute("success", "Account closed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error closing account: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/user/{userId}")
    public String getUserAccounts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Account> accounts = accountService.findByUser(user, pageRequest);
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("user", user);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accounts.getTotalPages());
        model.addAttribute("totalItems", accounts.getTotalElements());
        
        return "admin/accounts/user-accounts";
    }

    @GetMapping("/type/{type}")
    public String getAccountsByType(
            @PathVariable Account.AccountType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Account> accounts = accountService.findByType(type, pageRequest);
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("accountType", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accounts.getTotalPages());
        model.addAttribute("totalItems", accounts.getTotalElements());
        
        return "admin/accounts/type-accounts";
    }

    @GetMapping("/search")
    public String searchAccounts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Account> accounts = accountService.searchAccounts(query, pageRequest);
        
        model.addAttribute("accounts", accounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accounts.getTotalPages());
        model.addAttribute("totalItems", accounts.getTotalElements());
        model.addAttribute("query", query);
        
        return "admin/accounts/list";
    }
} 