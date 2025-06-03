package com.ebanca.service.impl;

import com.ebanca.model.Account;
import com.ebanca.model.Transaction;
import com.ebanca.model.TransactionStatistics;
import com.ebanca.model.User;
import com.ebanca.repository.TransactionRepository;
import com.ebanca.service.AccountService;
import com.ebanca.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransactionServiceImpl extends GenericServiceImpl<Transaction, Long> implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    @Override
    protected TransactionRepository getRepository() {
        return transactionRepository;
    }

    @Override
    public Page<Transaction> findBySourceAccount(Account account, Pageable pageable) {
        return transactionRepository.findBySourceAccount(account, pageable);
    }

    @Override
    public Page<Transaction> findByDestinationAccount(Account account, Pageable pageable) {
        return transactionRepository.findByTargetAccount(account, pageable);
    }

    @Override
    public Optional<Transaction> findByTransactionNumber(String transactionNumber) {
        return transactionRepository.findByTransactionNumber(transactionNumber);
    }

    @Override
    public List<Transaction> findBySourceAccountAndTransactionDateBetween(
            Account account, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findBySourceAccountAndCreatedAtBetween(account, startDate, endDate);
    }

    @Override
    public List<Transaction> findByDestinationAccountAndTransactionDateBetween(
            Account account, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTargetAccountAndCreatedAtBetween(account, startDate, endDate);
    }

    @Override
    public List<Transaction> findByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    @Override
    public Transaction createTransaction(Account sourceAccount, Account destinationAccount, 
            BigDecimal amount, String type) {
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(generateTransactionNumber());
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.valueOf(type));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setTimestamp(LocalDateTime.now());
        return save(transaction);
    }

    @Override
    public void updateTransactionStatus(Long transactionId, Transaction.TransactionStatus status) {
        Transaction transaction = findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setStatus(status);
        save(transaction);
    }

    @Override
    @Transactional
    public void processTransaction(Long transactionId) {
        Transaction transaction = findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new RuntimeException("Transaction is not in pending status");
        }

        try {
            switch (transaction.getType()) {
                case DEPOSIT:
                    accountService.updateBalance(transaction.getDestinationAccount().getId(), 
                        transaction.getAmount());
                    break;
                case WITHDRAWAL:
                    accountService.updateBalance(transaction.getSourceAccount().getId(), 
                        transaction.getAmount().negate());
                    break;
                case TRANSFER:
                    accountService.transfer(
                        transaction.getSourceAccount().getId(),
                        transaction.getDestinationAccount().getId(),
                        transaction.getAmount()
                    );
                    break;
                case LOAN_PAYMENT:
                    accountService.updateBalance(transaction.getSourceAccount().getId(), 
                        transaction.getAmount().negate());
                    break;
                case LOAN_DISBURSEMENT:
                    accountService.updateBalance(transaction.getDestinationAccount().getId(), 
                        transaction.getAmount());
                    break;
            }
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
            throw new RuntimeException("Transaction processing failed: " + e.getMessage());
        }
        save(transaction);
    }

    @Override
    public void cancelTransaction(Long transactionId) {
        Transaction transaction = findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new RuntimeException("Only pending transactions can be cancelled");
        }

        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        save(transaction);
    }

    @Override
    public void reverseTransaction(Long transactionId, String reason) {
        Transaction original = findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (original.getStatus() != Transaction.TransactionStatus.COMPLETED) {
            throw new RuntimeException("Only completed transactions can be reversed");
        }

        Transaction reversal = new Transaction();
        reversal.setTransactionNumber(generateTransactionNumber());
        reversal.setSourceAccount(original.getDestinationAccount());
        reversal.setDestinationAccount(original.getSourceAccount());
        reversal.setAmount(original.getAmount());
        reversal.setType(Transaction.TransactionType.TRANSFER);
        reversal.setStatus(Transaction.TransactionStatus.COMPLETED);
        reversal.setTimestamp(LocalDateTime.now());
        reversal.setReasonForReversal(reason);
        
        save(reversal);
        
        original.setStatus(Transaction.TransactionStatus.REVERSED);
        save(original);
    }

    @Override
    public Page<Transaction> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public Page<Transaction> findByAccount(Account account, Pageable pageable) {
        return transactionRepository.findBySourceAccountOrTargetAccount(account, account, pageable);
    }

    @Override
    public Page<Transaction> findByUser(User user, Pageable pageable) {
        return transactionRepository.findBySourceAccount_UserOrTargetAccount_User(user, user, pageable);
    }

    @Override
    public Page<Transaction> findByType(Transaction.TransactionType type, Pageable pageable) {
        return transactionRepository.findByType(type, pageable);
    }

    @Override
    public Page<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<Transaction> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return transactionRepository.findByAmountBetween(minAmount, maxAmount, pageable);
    }

    @Override
    public Page<Transaction> searchTransactions(String query, Pageable pageable) {
        return transactionRepository.findByTransactionNumberContainingOrDescriptionContaining(query, query, pageable);
    }

    @Override
    public List<Transaction> exportTransactions(LocalDateTime startDate, LocalDateTime endDate, 
            Transaction.TransactionType type, Long accountId, Long userId) {
        return transactionRepository.findByCreatedAtBetweenAndTypeAndSourceAccount_IdAndSourceAccount_User_Id(
            startDate, endDate, type, accountId, userId);
    }

    @Override
    public TransactionStatistics getTransactionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByCreatedAtBetween(startDate, endDate);
        
        if (transactions.isEmpty()) {
            return new TransactionStatistics(BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal totalAmount = transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long count = transactions.size();
        BigDecimal average = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        
        BigDecimal minAmount = transactions.stream()
            .map(Transaction::getAmount)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        
        BigDecimal maxAmount = transactions.stream()
            .map(Transaction::getAmount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        return new TransactionStatistics(totalAmount, count, average, minAmount, maxAmount);
    }

    private String generateTransactionNumber() {
        String transactionNumber;
        do {
            transactionNumber = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 12);
        } while (transactionRepository.existsByTransactionNumber(transactionNumber));
        return transactionNumber;
    }
}