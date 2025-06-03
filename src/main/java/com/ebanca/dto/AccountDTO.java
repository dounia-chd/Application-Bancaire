package com.ebanca.dto;

import com.ebanca.model.Account;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountDTO {
    private Long id;

    @NotBlank(message = "Le numéro de compte est obligatoire")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{10}$", message = "Le numéro de compte doit être au format XX1234567890")
    private String accountNumber;

    @NotNull(message = "Le type de compte est obligatoire")
    private Account.AccountType type;

    @NotNull(message = "Le solde est obligatoire")
    @DecimalMin(value = "0.0", message = "Le solde ne peut pas être négatif")
    private BigDecimal balance;

    @NotBlank(message = "Le statut est obligatoire")
    private String status;

    private Long userId;
    private String username;
    private String email;
} 