package com.ebanca.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends BusinessException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String accountNumber, BigDecimal required, BigDecimal available) {
        super(String.format("Fonds insuffisants sur le compte %s. Montant requis: %s, Disponible: %s",
            accountNumber, required, available));
    }
} 