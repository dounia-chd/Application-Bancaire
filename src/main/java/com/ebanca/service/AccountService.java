package com.ebanca.service;

import com.ebanca.model.Account;
import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountService extends GenericService<Account, Long> {
    List<Account> findByUser(User user);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUserAndActiveTrue(User user);
    boolean existsByAccountNumber(String accountNumber);
    List<Account> findByType(Account.AccountType type);
    Account createAccount(User user, Account.AccountType type);
    void deactivateAccount(Long accountId);
    void activateAccount(Long accountId);
    void updateBalance(Long accountId, BigDecimal amount);
    void transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount);
    Page<Account> findAll(Pageable pageable);
    Page<Account> findByUser(User user, Pageable pageable);
    Page<Account> findByType(Account.AccountType type, Pageable pageable);
    Page<Account> findByActive(boolean active, Pageable pageable);
    Page<Account> searchAccounts(String query, Pageable pageable);
    void deposit(Long accountId, BigDecimal amount);
    void withdraw(Long accountId, BigDecimal amount);
    Optional<Account> getAccountWithTransactions(Long accountId);
    Optional<Account> getAccountWithUser(Long accountId);
    void freezeAccount(Long id);
    void unfreezeAccount(Long id);
    void closeAccount(Long id);
} 