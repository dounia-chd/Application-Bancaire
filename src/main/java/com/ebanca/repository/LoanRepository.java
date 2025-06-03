package com.ebanca.repository;

import com.ebanca.model.Loan;
import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    Optional<Loan> findByLoanNumber(String loanNumber);
    List<Loan> findByStatus(Loan.LoanStatus status);
    Page<Loan> findByUserAndStatus(User user, Loan.LoanStatus status, Pageable pageable);
    List<Loan> findByStatusIn(List<Loan.LoanStatus> statuses);
    boolean existsByLoanNumber(String loanNumber);

    // New methods with pagination
    Page<Loan> findByUser(User user, Pageable pageable);
    Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable);
    Page<Loan> findByStatusIn(List<Loan.LoanStatus> statuses, Pageable pageable);
    Page<Loan> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    Page<Loan> findByRequestDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Loan> findByLoanNumberContainingOrPurposeContaining(String loanNumber, String purpose, Pageable pageable);

    // Custom queries for fetching related entities
    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.user WHERE l.id = :id")
    Optional<Loan> findByIdWithUser(Long id);

    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.account WHERE l.id = :id")
    Optional<Loan> findByIdWithAccount(Long id);

    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.transactions WHERE l.id = :id")
    Optional<Loan> findByIdWithTransactions(Long id);

    // Custom query for export
    @Query("SELECT l FROM Loan l WHERE l.requestDate BETWEEN :startDate AND :endDate AND l.status = :status AND l.user.id = :userId")
    List<Loan> findByRequestDateBetweenAndStatusAndUserId(LocalDateTime startDate, LocalDateTime endDate, Loan.LoanStatus status, Long userId);

    // Requêtes personnalisées pour les statistiques
    @Query("SELECT SUM(l.amount) FROM Loan l WHERE l.user = :user AND l.status = :status")
    BigDecimal sumLoansByUserAndStatus(User user, Loan.LoanStatus status);
    
    @Query("SELECT COUNT(l) FROM Loan l WHERE l.user = :user AND l.status = :status")
    Long countLoansByUserAndStatus(User user, Loan.LoanStatus status);
    
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.status = :status AND l.termMonths = :termMonths")
    List<Loan> findByUserAndStatusAndTermMonths(User user, Loan.LoanStatus status, int termMonths);
    
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.status = :status AND l.interestRate <= :maxInterestRate")
    List<Loan> findByUserAndStatusAndMaxInterestRate(User user, Loan.LoanStatus status, BigDecimal maxInterestRate);
    
    @Query("SELECT l FROM Loan l WHERE l.user = :user AND l.status = :status AND l.remainingAmount > 0")
    List<Loan> findActiveLoansByUserAndStatus(User user, Loan.LoanStatus status);
} 