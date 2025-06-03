package com.ebanca.controller;

import com.ebanca.model.Account;
import com.ebanca.model.User;
import com.ebanca.service.AccountService;
import com.ebanca.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Account>> getAllAccounts(Pageable pageable) {
        return ResponseEntity.ok(accountService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(#id)")
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return accountService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isUserOwner(#userId)")
    public ResponseEntity<Page<Account>> getUserAccounts(
            @PathVariable Long userId,
            Pageable pageable) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(accountService.findByUser(user, pageable));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Account>> getAccountsByType(
            @PathVariable Account.AccountType type,
            Pageable pageable) {
        return ResponseEntity.ok(accountService.findByType(type, pageable));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Account>> getActiveAccounts(
            @RequestParam(defaultValue = "true") boolean active,
            Pageable pageable) {
        return ResponseEntity.ok(accountService.findByActive(active, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Account> createAccount(
            @RequestParam Long userId,
            @RequestParam String type) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Account account = accountService.createAccount(user, Account.AccountType.valueOf(type));
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(#id)")
    public ResponseEntity<Void> deposit(
            @PathVariable Long id,
            @RequestParam double amount) {
        accountService.deposit(id, BigDecimal.valueOf(amount));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(#id)")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long id,
            @RequestParam double amount) {
        accountService.withdraw(id, BigDecimal.valueOf(amount));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(#id)")
    public ResponseEntity<Void> transfer(
            @PathVariable Long id,
            @RequestParam Long destinationAccountId,
            @RequestParam double amount) {
        accountService.transfer(id, destinationAccountId, BigDecimal.valueOf(amount));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateAccount(@PathVariable Long id) {
        accountService.activateAccount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateAccount(@PathVariable Long id) {
        accountService.deactivateAccount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Account>> searchAccounts(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(accountService.searchAccounts(query, pageable));
    }

    @GetMapping("/{id}/with-transactions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(#id)")
    public ResponseEntity<Account> getAccountWithTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountWithTransactions(id)
                .orElseThrow(() -> new RuntimeException("Account not found")));
    }

    @GetMapping("/{id}/with-user")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isAccountOwner(#id)")
    public ResponseEntity<Account> getAccountWithUser(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountWithUser(id)
                .orElseThrow(() -> new RuntimeException("Account not found")));
    }
} 