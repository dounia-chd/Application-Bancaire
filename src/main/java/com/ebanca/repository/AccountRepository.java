package com.ebanca.repository;

import com.ebanca.model.Account;
import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserAndStatus(User user, String status);
    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByType(Account.AccountType type);
    
    // Méthodes avec pagination
    Page<Account> findByUser(User user, Pageable pageable);
    Page<Account> findByType(Account.AccountType type, Pageable pageable);
    Page<Account> findByStatus(String status, Pageable pageable);
    Page<Account> findByAccountNumberContainingOrUser_UsernameContainingOrUser_EmailContaining(
        String accountNumber, String username, String email, Pageable pageable);
    
    // Requêtes personnalisées pour charger les relations
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.transactions WHERE a.id = :id")
    Optional<Account> findByIdWithTransactions(Long id);
    
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.user WHERE a.id = :id")
    Optional<Account> findByIdWithUser(Long id);
    
    // Méthodes de recherche avancée
    List<Account> findByBalanceGreaterThan(BigDecimal amount);
    List<Account> findByBalanceLessThan(BigDecimal amount);
    List<Account> findByBalanceBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Requêtes personnalisées pour les statistiques
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user = :user AND a.type = :type")
    BigDecimal sumBalanceByUserAndType(User user, Account.AccountType type);
    
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user = :user AND a.type = :type")
    Long countAccountsByUserAndType(User user, Account.AccountType type);
    
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.status = :status AND a.type = :type")
    List<Account> findByUserAndStatusAndType(User user, String status, Account.AccountType type);

    Page<Account> findByEnabled(boolean enabled, Pageable pageable);
} 