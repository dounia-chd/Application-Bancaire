package com.ebanca.service;

import com.ebanca.model.Loan;
import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoanService extends GenericService<Loan, Long> {
    Loan requestLoan(User user, BigDecimal amount, int termMonths, String purpose);
    void approveLoan(Long loanId);
    void rejectLoan(Long loanId, String reason);
    void makePayment(Long loanId, BigDecimal amount);
    void processLoanPayment(Long loanId, BigDecimal amount);
    List<Loan> findByUser(User user);
    List<Loan> findByStatus(Loan.LoanStatus status);
    Optional<Loan> findByLoanNumber(String loanNumber);
    Page<Loan> findByUserAndStatus(User user, Loan.LoanStatus status, Pageable pageable);
    List<Loan> findByStatusIn(List<Loan.LoanStatus> statuses);
    Page<Loan> findByStatusIn(List<Loan.LoanStatus> statuses, Pageable pageable);
    Page<Loan> findAll(Pageable pageable);
    Page<Loan> findByUser(User user, Pageable pageable);
    Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable);
    Page<Loan> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    Page<Loan> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Loan> searchLoans(String query, Pageable pageable);
    Optional<Loan> getLoanWithUser(Long loanId);
    Optional<Loan> getLoanWithAccount(Long loanId);
    Optional<Loan> getLoanWithTransactions(Long loanId);
    void markLoanAsDefaulted(Long loanId);
    void markLoanAsPaid(Long loanId);
    List<Loan> exportLoans(LocalDateTime startDate, LocalDateTime endDate, Loan.LoanStatus status, Long userId);
    double calculateMonthlyPayment(double amount, double interestRate, int termMonths);
    double calculateTotalPayment(double amount, double interestRate, int termMonths);
} 