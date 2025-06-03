package com.ebanca.controller;

import com.ebanca.model.Account;
import com.ebanca.model.Transaction;
import com.ebanca.model.TransactionStatistics;
import com.ebanca.service.AccountService;
import com.ebanca.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Transaction>> getAllTransactions(Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id)")
    public ResponseEntity<Transaction> getTransaction(@PathVariable Long id) {
        return transactionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(#accountId)")
    public ResponseEntity<Page<Transaction>> getAccountTransactions(
            @PathVariable Long accountId,
            Pageable pageable) {
        Account account = accountService.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return ResponseEntity.ok(transactionService.findByAccount(account, pageable));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Transaction>> getTransactionsByType(
            @PathVariable Transaction.TransactionType type,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByType(type, pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable Transaction.TransactionStatus status) {
        return ResponseEntity.ok(transactionService.findByStatus(status));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Transaction>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/amount-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Transaction>> getTransactionsByAmountRange(
            @RequestParam double minAmount,
            @RequestParam double maxAmount,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.findByAmountRange(
            BigDecimal.valueOf(minAmount), 
            BigDecimal.valueOf(maxAmount), 
            pageable));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isTransactionOwner(#id)")
    public ResponseEntity<Void> cancelTransaction(@PathVariable Long id) {
        transactionService.cancelTransaction(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reverseTransaction(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        transactionService.reverseTransaction(id, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Transaction>> searchTransactions(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.searchTransactions(query, pageable));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transaction>> exportTransactions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Transaction.TransactionType type,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(transactionService.exportTransactions(startDate, endDate, type, accountId, userId));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionStatistics> getTransactionStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(transactionService.getTransactionStatistics(startDate, endDate));
    }
} 