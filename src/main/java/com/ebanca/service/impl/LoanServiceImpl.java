package com.ebanca.service.impl;

import com.ebanca.model.Account;
import com.ebanca.model.Loan;
import com.ebanca.model.User;
import com.ebanca.repository.LoanRepository;
import com.ebanca.service.AccountService;
import com.ebanca.service.LoanService;
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
public class LoanServiceImpl extends GenericServiceImpl<Loan, Long> implements LoanService {

    private final LoanRepository loanRepository;
    private final AccountService accountService;

    public LoanServiceImpl(LoanRepository loanRepository, AccountService accountService) {
        this.loanRepository = loanRepository;
        this.accountService = accountService;
    }

    @Override
    protected LoanRepository getRepository() {
        return loanRepository;
    }

    @Override
    public List<Loan> findByUser(User user) {
        return loanRepository.findByUser(user);
    }

    @Override
    public Optional<Loan> findByLoanNumber(String loanNumber) {
        return loanRepository.findByLoanNumber(loanNumber);
    }

    @Override
    public List<Loan> findByStatus(Loan.LoanStatus status) {
        return loanRepository.findByStatus(status);
    }

    @Override
    public Page<Loan> findByUserAndStatus(User user, Loan.LoanStatus status, Pageable pageable) {
        return loanRepository.findByUserAndStatus(user, status, pageable);
    }

    @Override
    public List<Loan> findByStatusIn(List<Loan.LoanStatus> statuses) {
        return loanRepository.findByStatusIn(statuses);
    }

    @Override
    public Loan requestLoan(User user, BigDecimal amount, int termMonths, String purpose) {
        Loan loan = new Loan();
        loan.setLoanNumber(generateLoanNumber());
        loan.setUser(user);
        loan.setAmount(amount);
        loan.setInterestRate(BigDecimal.valueOf(5.0)); // 5% interest rate by default
        loan.setTermMonths(termMonths);
        loan.setPurpose(purpose);
        loan.setMonthlyPayment(BigDecimal.valueOf(calculateMonthlyPayment(amount.doubleValue(), 5.0, termMonths)));
        loan.setTotalPayment(BigDecimal.valueOf(calculateTotalPayment(amount.doubleValue(), 5.0, termMonths)));
        loan.setRemainingAmount(amount);
        loan.setStatus(Loan.LoanStatus.PENDING);
        loan.setRequestDate(LocalDateTime.now());
        return save(loan);
    }

    @Override
    @Transactional
    public void approveLoan(Long loanId) {
        Loan loan = findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RuntimeException("Only pending loans can be approved");
        }

        // Create a loan account for the user
        Account loanAccount = accountService.createAccount(loan.getUser(), Account.AccountType.LOAN);
        loan.setAccount(loanAccount);
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        save(loan);

        // Credit the loan amount to the user's account
        accountService.updateBalance(loanAccount.getId(), loan.getAmount());
    }

    @Override
    public void rejectLoan(Long loanId, String reason) {
        Loan loan = findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));
        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setRejectionReason(reason);
        loan.setRejectionDate(LocalDateTime.now());
        save(loan);
    }

    @Override
    @Transactional
    public void processLoanPayment(Long loanId, BigDecimal amount) {
        Loan loan = findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new RuntimeException("Only active loans can receive payments");
        }

        BigDecimal newBalance = loan.getRemainingAmount().subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Payment amount exceeds remaining balance");
        }

        loan.setRemainingAmount(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(Loan.LoanStatus.PAID);
            loan.setEndDate(LocalDateTime.now());
        }

        save(loan);
    }

    @Override
    public void markLoanAsPaid(Long loanId) {
        Loan loan = findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new RuntimeException("Only active loans can be marked as paid");
        }

        loan.setStatus(Loan.LoanStatus.PAID);
        loan.setEndDate(LocalDateTime.now());
        loan.setRemainingAmount(BigDecimal.ZERO);
        save(loan);
    }

    @Override
    public void markLoanAsDefaulted(Long loanId) {
        Loan loan = findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new RuntimeException("Only active loans can be marked as defaulted");
        }

        loan.setStatus(Loan.LoanStatus.DEFAULTED);
        save(loan);
    }

    @Override
    public double calculateMonthlyPayment(double amount, double interestRate, int termMonths) {
        double monthlyRate = interestRate / 12 / 100;
        double monthlyPayment = (amount * monthlyRate * Math.pow(1 + monthlyRate, termMonths))
            / (Math.pow(1 + monthlyRate, termMonths) - 1);
        return BigDecimal.valueOf(monthlyPayment)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    @Override
    public double calculateTotalPayment(double amount, double interestRate, int termMonths) {
        double monthlyPayment = calculateMonthlyPayment(amount, interestRate, termMonths);
        return BigDecimal.valueOf(monthlyPayment * termMonths)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    @Override
    public Page<Loan> findAll(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    @Override
    public Page<Loan> findByUser(User user, Pageable pageable) {
        return loanRepository.findByUser(user, pageable);
    }

    @Override
    public Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable) {
        return loanRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Loan> findByStatusIn(List<Loan.LoanStatus> statuses, Pageable pageable) {
        return loanRepository.findByStatusIn(statuses, pageable);
    }

    @Override
    public Page<Loan> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return loanRepository.findByAmountBetween(minAmount, maxAmount, pageable);
    }

    @Override
    public Page<Loan> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return loanRepository.findByRequestDateBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<Loan> searchLoans(String query, Pageable pageable) {
        return loanRepository.findByLoanNumberContainingOrPurposeContaining(query, query, pageable);
    }

    @Override
    public Optional<Loan> getLoanWithUser(Long loanId) {
        return loanRepository.findByIdWithUser(loanId);
    }

    @Override
    public Optional<Loan> getLoanWithAccount(Long loanId) {
        return loanRepository.findByIdWithAccount(loanId);
    }

    @Override
    public Optional<Loan> getLoanWithTransactions(Long loanId) {
        return loanRepository.findByIdWithTransactions(loanId);
    }

    @Override
    public List<Loan> exportLoans(LocalDateTime startDate, LocalDateTime endDate, Loan.LoanStatus status, Long userId) {
        return loanRepository.findByRequestDateBetweenAndStatusAndUserId(startDate, endDate, status, userId);
    }

    @Override
    public void makePayment(Long loanId, BigDecimal amount) {
        processLoanPayment(loanId, amount);
    }

    private String generateLoanNumber() {
        String loanNumber;
        do {
            loanNumber = UUID.randomUUID().toString().replaceAll("[^0-9]", "").substring(0, 8);
        } while (loanRepository.existsByLoanNumber(loanNumber));
        return loanNumber;
    }
} 