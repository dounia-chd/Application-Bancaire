package com.ebanca.dto;

import com.ebanca.model.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    private Long id;

    @NotBlank(message = "Le numéro de transaction est obligatoire")
    @Pattern(regexp = "^TRX[0-9]{10}$", message = "Le numéro de transaction doit être au format TRX1234567890")
    private String transactionNumber;

    @NotNull(message = "Le type de transaction est obligatoire")
    private Transaction.TransactionType type;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, max = 255, message = "La description doit contenir entre 5 et 255 caractères")
    private String description;

    @NotNull(message = "Le compte source est obligatoire")
    private Long sourceAccountId;

    private Long targetAccountId;

    @NotNull(message = "Le statut est obligatoire")
    private Transaction.TransactionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Informations supplémentaires pour l'affichage
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private String sourceUsername;
    private String targetUsername;
} 