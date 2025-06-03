package com.ebanca.integration;

import com.ebanca.model.Account;
import com.ebanca.model.Transaction;
import com.ebanca.model.User;
import com.ebanca.repository.AccountRepository;
import com.ebanca.repository.TransactionRepository;
import com.ebanca.repository.UserRepository;
import com.ebanca.service.AccountService;
import com.ebanca.service.TransactionService;
import com.ebanca.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données avant chaque test
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur de test
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("Password123!");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser = userService.createUser(testUser);

        // Créer les comptes de test
        sourceAccount = accountService.createAccount(testUser, Account.AccountType.CHECKING);
        destinationAccount = accountService.createAccount(testUser, Account.AccountType.SAVINGS);

        // Dépôt initial sur le compte source
        accountService.deposit(sourceAccount.getId(), new BigDecimal("1000.00"));
    }

    @Test
    void whenCreateTransaction_thenTransactionIsSaved() {
        Transaction transaction = transactionService.createTransaction(
            sourceAccount,
            destinationAccount,
            new BigDecimal("100.00"),
            "TRANSFER"
        );

        assertNotNull(transaction.getId());
        assertEquals(sourceAccount.getId(), transaction.getSourceAccount().getId());
        assertEquals(destinationAccount.getId(), transaction.getDestinationAccount().getId());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(Transaction.TransactionType.TRANSFER, transaction.getType());
        assertEquals(Transaction.TransactionStatus.PENDING, transaction.getStatus());
    }

    @Test
    void whenProcessTransaction_thenBalancesAreUpdated() {
        // Créer et traiter une transaction
        Transaction transaction = transactionService.createTransaction(
            sourceAccount,
            destinationAccount,
            new BigDecimal("100.00"),
            "TRANSFER"
        );
        transactionService.processTransaction(transaction.getId());

        // Vérifier les soldes mis à jour
        Account updatedSourceAccount = accountService.findById(sourceAccount.getId()).orElseThrow();
        Account updatedDestinationAccount = accountService.findById(destinationAccount.getId()).orElseThrow();

        assertEquals(new BigDecimal("900.00"), updatedSourceAccount.getBalance());
        assertEquals(new BigDecimal("100.00"), updatedDestinationAccount.getBalance());

        // Vérifier le statut de la transaction
        Transaction processedTransaction = transactionService.findById(transaction.getId()).orElseThrow();
        assertEquals(Transaction.TransactionStatus.COMPLETED, processedTransaction.getStatus());
    }

    @Test
    void whenFindByAccount_thenTransactionsAreReturned() {
        // Créer quelques transactions
        Transaction transaction1 = transactionService.createTransaction(
            sourceAccount,
            destinationAccount,
            new BigDecimal("100.00"),
            "TRANSFER"
        );
        Transaction transaction2 = transactionService.createTransaction(
            destinationAccount,
            sourceAccount,
            new BigDecimal("50.00"),
            "TRANSFER"
        );

        // Traiter les transactions
        transactionService.processTransaction(transaction1.getId());
        transactionService.processTransaction(transaction2.getId());

        // Vérifier les transactions du compte source
        var sourceTransactions = transactionService.findByAccount(sourceAccount, PageRequest.of(0, 10)).getContent();
        assertEquals(2, sourceTransactions.size());

        // Vérifier les transactions du compte destination
        var destinationTransactions = transactionService.findByAccount(destinationAccount, PageRequest.of(0, 10)).getContent();
        assertEquals(2, destinationTransactions.size());
    }

    @Test
    void whenFindByStatus_thenTransactionsAreReturned() {
        // Créer une transaction
        Transaction transaction = transactionService.createTransaction(
            sourceAccount,
            destinationAccount,
            new BigDecimal("100.00"),
            "TRANSFER"
        );

        // Vérifier les transactions en attente
        List<Transaction> pendingTransactions = transactionService.findByStatus(Transaction.TransactionStatus.PENDING);
        assertEquals(1, pendingTransactions.size());

        // Traiter la transaction
        transactionService.processTransaction(transaction.getId());

        // Vérifier les transactions complétées
        List<Transaction> completedTransactions = transactionService.findByStatus(Transaction.TransactionStatus.COMPLETED);
        assertEquals(1, completedTransactions.size());
    }

    @Test
    void whenProcessTransactionFails_thenTransactionIsMarkedAsFailed() {
        // Créer une transaction avec un montant supérieur au solde
        Transaction transaction = transactionService.createTransaction(
            sourceAccount,
            destinationAccount,
            new BigDecimal("2000.00"),
            "TRANSFER"
        );

        // Tenter de traiter la transaction
        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(transaction.getId()));

        // Vérifier que la transaction est marquée comme échouée
        Transaction failedTransaction = transactionService.findById(transaction.getId()).orElseThrow();
        assertEquals(Transaction.TransactionStatus.FAILED, failedTransaction.getStatus());
        assertNotNull(failedTransaction.getErrorMessage());
    }

    @Test
    void whenFindByDateRange_thenTransactionsAreReturned() {
        // Créer et traiter une transaction
        Transaction transaction = transactionService.createTransaction(
            sourceAccount,
            destinationAccount,
            new BigDecimal("100.00"),
            "TRANSFER"
        );
        transactionService.processTransaction(transaction.getId());

        // Rechercher les transactions dans une plage de dates
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        var transactions = transactionService.findByDateRange(startDate, endDate, PageRequest.of(0, 10)).getContent();

        assertEquals(1, transactions.size());
        assertEquals(transaction.getId(), transactions.get(0).getId());
    }
} 