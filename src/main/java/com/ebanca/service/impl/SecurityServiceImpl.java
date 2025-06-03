package com.ebanca.service.impl;

import com.ebanca.model.User;
import com.ebanca.repository.AccountRepository;
import com.ebanca.repository.LoanRepository;
import com.ebanca.repository.TransactionRepository;
import com.ebanca.repository.UserRepository;
import com.ebanca.service.SecurityService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;

    public SecurityServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LoanRepository loanRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public boolean isCurrentUser(Long userId) {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getId().equals(userId);
    }

    @Override
    public boolean isUserOwner(Long userId) {
        return isCurrentUser(userId);
    }

    @Override
    public boolean isAccountOwner(Long accountId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return accountRepository.findById(accountId)
            .map(account -> account.getUser().getId().equals(currentUser.getId()))
            .orElse(false);
    }

    @Override
    public boolean isTransactionOwner(Long transactionId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return transactionRepository.findById(transactionId)
            .<Boolean>map(transaction -> transaction.getSourceAccount().getUser().getId().equals(currentUser.getId()) ||
                (transaction.getDestinationAccount() != null && 
                 transaction.getDestinationAccount().getUser().getId().equals(currentUser.getId())))
            .orElse(false);
    }

    @Override
    public boolean isLoanOwner(Long loanId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return loanRepository.findById(loanId)
            .map(loan -> loan.getUser().getId().equals(currentUser.getId()))
            .orElse(false);
    }

    @Override
    public boolean hasRole(String role) {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRoles().stream()
            .anyMatch(r -> r.getName().equals(role));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
} 