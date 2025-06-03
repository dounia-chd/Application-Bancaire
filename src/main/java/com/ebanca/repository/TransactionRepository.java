package com.ebanca.repository;

import com.ebanca.model.Account;
import com.ebanca.model.Transaction;
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
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findBySourceAccount(Account account, Pageable pageable);
    Page<Transaction> findByTargetAccount(Account account, Pageable pageable);
    Optional<Transaction> findByTransactionNumber(String transactionNumber);
    List<Transaction> findBySourceAccountAndCreatedAtBetween(
        Account account, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByTargetAccountAndCreatedAtBetween(
        Account account, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    boolean existsByTransactionNumber(String transactionNumber);
    
    // Méthodes avec pagination
    Page<Transaction> findBySourceAccountOrTargetAccount(Account sourceAccount, Account targetAccount, Pageable pageable);
    Page<Transaction> findBySourceAccount_UserOrTargetAccount_User(User sourceUser, User targetUser, Pageable pageable);
    Page<Transaction> findByType(Transaction.TransactionType type, Pageable pageable);
    Page<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Page<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    Page<Transaction> findByTransactionNumberContainingOrDescriptionContaining(String transactionNumber, String description, Pageable pageable);
    
    // Méthodes de recherche avancée
    List<Transaction> findBySourceAccountOrTargetAccount(Account sourceAccount, Account targetAccount);
    List<Transaction> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    // Requêtes personnalisées pour l'export et les statistiques
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate " +
           "AND t.type = :type AND t.sourceAccount.id = :accountId AND t.sourceAccount.user.id = :userId")
    List<Transaction> findTransactionsForExport(
        LocalDateTime startDate, LocalDateTime endDate, Transaction.TransactionType type, Long accountId, Long userId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.sourceAccount = :account " +
           "AND t.createdAt BETWEEN :startDate AND :endDate AND t.type = :type")
    BigDecimal sumTransactionsByAccountAndDateRange(
        Account account, LocalDateTime startDate, LocalDateTime endDate, Transaction.TransactionType type);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.sourceAccount = :account " +
           "AND t.createdAt BETWEEN :startDate AND :endDate AND t.type = :type")
    Long countTransactionsByAccountAndDateRange(
        Account account, LocalDateTime startDate, LocalDateTime endDate, Transaction.TransactionType type);

    Page<Transaction> findByDestinationAccount(Account account, Pageable pageable);
    Page<Transaction> findByAccount(Account account, Pageable pageable);
    Page<Transaction> findByUser(Account account, Pageable pageable);
    Page<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Transaction> searchTransactions(String query, Pageable pageable);
    List<Transaction> findByCreatedAtBetweenAndTypeAndSourceAccount_IdAndSourceAccount_User_Id(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Transaction.TransactionType type, 
        Long accountId, 
        Long userId
    );
} 