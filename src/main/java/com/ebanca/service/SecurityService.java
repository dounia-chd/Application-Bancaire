package com.ebanca.service;

import com.ebanca.model.User;

public interface SecurityService {
    boolean isCurrentUser(Long userId);
    boolean isUserOwner(Long userId);
    boolean isAccountOwner(Long accountId);
    boolean isTransactionOwner(Long transactionId);
    boolean isLoanOwner(Long loanId);
    boolean hasRole(String role);
    User getCurrentUser();
} 