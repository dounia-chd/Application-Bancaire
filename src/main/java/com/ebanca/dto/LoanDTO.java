package com.ebanca.dto;

import com.ebanca.model.Loan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanDTO {
    private Long id;

    @NotBlank(message = "Le numéro de prêt est obligatoire")
    @Pattern(regexp = "^LOAN[0-9]{10}$", message = "Le numéro de prêt doit être au format LOAN1234567890")
    private String loanNumber;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "100.0", message = "Le montant minimum du prêt est de 100")
    private BigDecimal amount;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 1, message = "La durée doit être d'au moins 1 mois")
    private int termMonths;

    @NotNull(message = "Le taux d'intérêt est obligatoire")
    @DecimalMin(value = "0.0", message = "Le taux d'intérêt ne peut pas être négatif")
    private BigDecimal interestRate;

    @NotBlank(message = "Le motif est obligatoire")
    @Size(min = 10, max = 500, message = "Le motif doit contenir entre 10 et 500 caractères")
    private String purpose;

    @NotNull(message = "Le statut est obligatoire")
    private Loan.LoanStatus status;

    private BigDecimal remainingAmount;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private LocalDateTime paidDate;
    private String rejectionReason;
    private LocalDateTime rejectionDate;

    // Informations supplémentaires pour l'affichage
    private Long userId;
    private String username;
    private String email;
    private Long accountId;
    private String accountNumber;
} 