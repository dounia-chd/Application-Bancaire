package com.ebanca.service.impl;

import com.ebanca.model.Account;
import com.ebanca.model.User;
import com.ebanca.repository.AccountRepository;
import com.ebanca.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AccountServiceImpl extends GenericServiceImpl<Account, Long> implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    protected AccountRepository getRepository() {
        return accountRepository;
    }

    @Override
    public List<Account> findByUser(User user) {
        return accountRepository.findByUser(user);
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public List<Account> findByUserAndActiveTrue(User user) {
        return accountRepository.findByUserAndStatus(user, "ACTIVE");
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    public List<Account> findByType(Account.AccountType type) {
        return accountRepository.findByType(type);
    }

    @Override
    public Account createAccount(User user, Account.AccountType type) {
        Account account = new Account();
        account.setUser(user);
        account.setType(type);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        return save(account);
    }

    @Override
    public void deactivateAccount(Long accountId) {
        Account account = findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus("INACTIVE");
        save(account);
    }

    @Override
    public void activateAccount(Long accountId) {
        Account account = findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus("ACTIVE");
        save(account);
    }

    @Override
    public void updateBalance(Long accountId, BigDecimal amount) {
        Account account = findById(accountId)
            .orElseThrow(() -> new RuntimeException("Account not found"));
        
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(newBalance);
        save(account);
    }

    @Override
    @Transactional
    public void transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be positive");
        }

        Account sourceAccount = findById(sourceAccountId)
            .orElseThrow(() -> new RuntimeException("Source account not found"));
        Account destinationAccount = findById(destinationAccountId)
            .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (!"ACTIVE".equals(sourceAccount.getStatus()) || !"ACTIVE".equals(destinationAccount.getStatus())) {
            throw new RuntimeException("One or both accounts are not active");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in source account");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        save(sourceAccount);
        save(destinationAccount);
    }

    @Override
    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Override
    public Page<Account> findByUser(User user, Pageable pageable) {
        return accountRepository.findByUser(user, pageable);
    }

    @Override
    public Page<Account> findByType(Account.AccountType type, Pageable pageable) {
        return accountRepository.findByType(type, pageable);
    }

    @Override
    public Page<Account> findByActive(boolean active, Pageable pageable) {
        return accountRepository.findByStatus(active ? "ACTIVE" : "INACTIVE", pageable);
    }

    @Override
    public Page<Account> searchAccounts(String query, Pageable pageable) {
        return accountRepository.findByAccountNumberContainingOrUser_UsernameContainingOrUser_EmailContaining(
            query, query, query, pageable);
    }

    @Override
    public void deposit(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }
        updateBalance(accountId, amount);
    }

    @Override
    public void withdraw(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive");
        }
        updateBalance(accountId, amount.negate());
    }

    @Override
    public Optional<Account> getAccountWithTransactions(Long accountId) {
        return accountRepository.findByIdWithTransactions(accountId);
    }

    @Override
    public Optional<Account> getAccountWithUser(Long accountId) {
        return accountRepository.findByIdWithUser(accountId);
    }

    @Override
    public void freezeAccount(Long id) {
        Account account = findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus("FROZEN");
        save(account);
    }

    @Override
    public void unfreezeAccount(Long id) {
        Account account = findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus("ACTIVE");
        save(account);
    }

    @Override
    public void closeAccount(Long id) {
        Account account = findById(id).orElseThrow(() -> new RuntimeException("Account not found"));
        account.setStatus("CLOSED");
        save(account);
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 10);
        } while (existsByAccountNumber(accountNumber));
        return accountNumber;
    }
} 