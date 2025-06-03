package com.ebanca.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private String loanNumber;
    private BigDecimal amount;
    private BigDecimal remainingAmount;
    private BigDecimal monthlyPayment;
    private BigDecimal totalPayment;
    private int termMonths;
    private String purpose;
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private LocalDateTime rejectionDate;
    private LocalDateTime paidDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum LoanStatus {
        PENDING,
        APPROVED,
        REJECTED,
        PAID,
        DEFAULTED,
        ACTIVE
    }
} 