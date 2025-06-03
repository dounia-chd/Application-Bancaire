package com.ebanca.controller;

import com.ebanca.model.Loan;
import com.ebanca.model.User;
import com.ebanca.service.LoanService;
import com.ebanca.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;

    public LoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Loan>> getAllLoans(Pageable pageable) {
        return ResponseEntity.ok(loanService.findAll(pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isCurrentUser(#userId)")
    public ResponseEntity<Page<Loan>> getUserLoans(
            @PathVariable Long userId,
            Pageable pageable) {
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(loanService.findByUser(user, pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Loan>> getLoansByStatus(
            @PathVariable Loan.LoanStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(loanService.findByStatus(status, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Loan>> searchLoans(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(loanService.searchLoans(query, pageable));
    }

    @GetMapping("/amount-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Loan>> getLoansByAmountRange(
            @RequestParam double minAmount,
            @RequestParam double maxAmount,
            Pageable pageable) {
        return ResponseEntity.ok(loanService.findByAmountRange(
            BigDecimal.valueOf(minAmount),
            BigDecimal.valueOf(maxAmount),
            pageable));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Loan>> getLoansByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(loanService.findByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isLoanOwner(#id)")
    public ResponseEntity<Loan> getLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.findById(id)
            .orElseThrow(() -> new RuntimeException("Loan not found")));
    }

    @GetMapping("/{id}/with-user")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isLoanOwner(#id)")
    public ResponseEntity<Loan> getLoanWithUser(@PathVariable Long id) {
        return loanService.getLoanWithUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-account")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isLoanOwner(#id)")
    public ResponseEntity<Loan> getLoanWithAccount(@PathVariable Long id) {
        return loanService.getLoanWithAccount(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/with-transactions")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isLoanOwner(#id)")
    public ResponseEntity<Loan> getLoanWithTransactions(@PathVariable Long id) {
        return loanService.getLoanWithTransactions(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Loan> createLoanRequest(
            @RequestParam Long userId,
            @RequestParam double amount,
            @RequestParam int termMonths,
            @RequestParam double interestRate) {
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(loanService.requestLoan(user, BigDecimal.valueOf(amount), termMonths, String.valueOf(interestRate)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveLoan(@PathVariable Long id) {
        loanService.approveLoan(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectLoan(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        loanService.rejectLoan(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isLoanOwner(#id)")
    public ResponseEntity<Void> processLoanPayment(
            @PathVariable Long id,
            @RequestParam double amount) {
        loanService.processLoanPayment(id, BigDecimal.valueOf(amount));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markLoanAsPaid(@PathVariable Long id) {
        loanService.markLoanAsPaid(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/mark-defaulted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markLoanAsDefaulted(@PathVariable Long id) {
        loanService.markLoanAsDefaulted(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calculate")
    public ResponseEntity<LoanCalculationResponse> calculateLoanDetails(
            @RequestParam double amount,
            @RequestParam double interestRate,
            @RequestParam int termMonths) {
        double monthlyPayment = loanService.calculateMonthlyPayment(amount, interestRate, termMonths);
        double totalPayment = loanService.calculateTotalPayment(amount, interestRate, termMonths);
        return ResponseEntity.ok(new LoanCalculationResponse(monthlyPayment, totalPayment));
    }

    private record LoanCalculationResponse(double monthlyPayment, double totalPayment) {}
} 