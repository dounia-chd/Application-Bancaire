package com.ebanca.service;

import com.ebanca.model.Account;
import com.ebanca.model.Transaction;
import com.ebanca.model.TransactionStatistics;
import com.ebanca.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionService extends GenericService<Transaction, Long> {
    Page<Transaction> findBySourceAccount(Account account, Pageable pageable);
    Page<Transaction> findByDestinationAccount(Account account, Pageable pageable);
    Optional<Transaction> findByTransactionNumber(String transactionNumber);
    List<Transaction> findBySourceAccountAndTransactionDateBetween(Account account, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByDestinationAccountAndTransactionDateBetween(Account account, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
    Transaction createTransaction(Account sourceAccount, Account destinationAccount, BigDecimal amount, String type);
    void updateTransactionStatus(Long transactionId, Transaction.TransactionStatus status);
    void processTransaction(Long transactionId);
    void cancelTransaction(Long transactionId);
    void reverseTransaction(Long transactionId, String reason);
    Page<Transaction> findAll(Pageable pageable);
    Page<Transaction> findByAccount(Account account, Pageable pageable);
    Page<Transaction> findByUser(User user, Pageable pageable);
    Page<Transaction> findByType(Transaction.TransactionType type, Pageable pageable);
    Page<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Transaction> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);
    Page<Transaction> searchTransactions(String query, Pageable pageable);
    List<Transaction> exportTransactions(LocalDateTime startDate, LocalDateTime endDate, Transaction.TransactionType type, Long accountId, Long userId);
    TransactionStatistics getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate);
} 