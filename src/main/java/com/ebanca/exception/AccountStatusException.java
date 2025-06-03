package com.ebanca.exception;

public class AccountStatusException extends BusinessException {
    public AccountStatusException(String message) {
        super(message);
    }

    public AccountStatusException(String accountNumber, String currentStatus, String requiredStatus) {
        super(String.format("Le compte %s est %s. Statut requis: %s",
            accountNumber, currentStatus, requiredStatus));
    }
} 