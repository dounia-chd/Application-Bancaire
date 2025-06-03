package com.ebanca.controller.admin;

import com.ebanca.model.Account;
import com.ebanca.model.Transaction;
import com.ebanca.model.User;
import com.ebanca.service.AccountService;
import com.ebanca.service.TransactionService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/transactions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final UserService userService;

    public AdminTransactionController(
            TransactionService transactionService,
            AccountService accountService,
            UserService userService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public String listTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Transaction> transactions = transactionService.findAll(pageRequest);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        
        return "admin/transactions/list";
    }

    @GetMapping("/{id}")
    public String viewTransaction(@PathVariable Long id, Model model) {
        Transaction transaction = transactionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        model.addAttribute("transaction", transaction);
        return "admin/transactions/view";
    }

    @GetMapping("/account/{accountId}")
    public String getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Account account = accountService.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionService.findByAccount(account, pageRequest);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("account", account);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        
        return "admin/transactions/account-transactions";
    }

    @GetMapping("/user/{userId}")
    public String getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionService.findByUser(user, pageRequest);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("user", user);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        
        return "admin/transactions/user-transactions";
    }

    @GetMapping("/type/{type}")
    public String getTransactionsByType(
            @PathVariable Transaction.TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionService.findByType(type, pageRequest);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("transactionType", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        
        return "admin/transactions/type-transactions";
    }

    @GetMapping("/date-range")
    public String getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionService.findByDateRange(startDate, endDate, pageRequest);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        
        return "admin/transactions/date-range-transactions";
    }

    @GetMapping("/amount-range")
    public String getTransactionsByAmountRange(
            @RequestParam double minAmount,
            @RequestParam double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionService.findByAmountRange(
            BigDecimal.valueOf(minAmount), 
            BigDecimal.valueOf(maxAmount), 
            pageRequest);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("maxAmount", maxAmount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        
        return "admin/transactions/amount-range-transactions";
    }

    @GetMapping("/search")
    public String searchTransactions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Transaction> transactions = transactionService.searchTransactions(query, pageRequest);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        model.addAttribute("query", query);
        
        return "admin/transactions/list";
    }

    @PostMapping("/{id}/reverse")
    public String reverseTransaction(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        try {
            transactionService.reverseTransaction(id, reason);
            redirectAttributes.addFlashAttribute("success", "Transaction reversed successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error reversing transaction: " + e.getMessage());
        }
        return "redirect:/admin/transactions/" + id;
    }

    @GetMapping("/export")
    public String exportTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Transaction.TransactionType type,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long userId,
            Model model) {
        
        List<Transaction> transactions = transactionService.exportTransactions(startDate, endDate, type, accountId, userId);
        model.addAttribute("transactions", transactions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("type", type);
        model.addAttribute("accountId", accountId);
        model.addAttribute("userId", userId);
        
        return "admin/transactions/export";
    }
} 