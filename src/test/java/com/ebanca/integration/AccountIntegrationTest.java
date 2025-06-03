package com.ebanca.integration;

import com.ebanca.exception.InsufficientFundsException;
import com.ebanca.model.Account;
import com.ebanca.model.User;
import com.ebanca.repository.AccountRepository;
import com.ebanca.repository.UserRepository;
import com.ebanca.service.AccountService;
import com.ebanca.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AccountIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Nettoyer la base de données avant chaque test
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

        // Créer un compte de test
        testAccount = accountService.createAccount(testUser, Account.AccountType.CHECKING);
    }

    @Test
    void whenCreateAccount_thenAccountIsSaved() {
        Account account = accountService.createAccount(testUser, Account.AccountType.SAVINGS);

        assertNotNull(account.getId());
        assertEquals(Account.AccountType.SAVINGS, account.getType());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("ACTIVE", account.getStatus());
        assertEquals(testUser.getId(), account.getUser().getId());
    }

    @Test
    void whenDeposit_thenBalanceIsUpdated() {
        BigDecimal amount = new BigDecimal("100.00");
        accountService.deposit(testAccount.getId(), amount);

        Account updatedAccount = accountService.findById(testAccount.getId()).orElseThrow();
        assertEquals(amount, updatedAccount.getBalance());
    }

    @Test
    void whenWithdrawWithSufficientFunds_thenBalanceIsUpdated() {
        // D'abord déposer de l'argent
        accountService.deposit(testAccount.getId(), new BigDecimal("200.00"));

        // Ensuite retirer
        BigDecimal withdrawAmount = new BigDecimal("100.00");
        accountService.withdraw(testAccount.getId(), withdrawAmount);

        Account updatedAccount = accountService.findById(testAccount.getId()).orElseThrow();
        assertEquals(new BigDecimal("100.00"), updatedAccount.getBalance());
    }

    @Test
    void whenWithdrawWithInsufficientFunds_thenThrowException() {
        assertThrows(InsufficientFundsException.class, () -> 
            accountService.withdraw(testAccount.getId(), new BigDecimal("100.00")));
    }

    @Test
    void whenTransfer_thenBalancesAreUpdated() {
        // Créer un deuxième compte
        Account targetAccount = accountService.createAccount(testUser, Account.AccountType.SAVINGS);

        // Dépôt sur le compte source
        accountService.deposit(testAccount.getId(), new BigDecimal("200.00"));

        // Transfert
        BigDecimal transferAmount = new BigDecimal("100.00");
        accountService.transfer(testAccount.getId(), targetAccount.getId(), transferAmount);

        // Vérifier les soldes
        Account updatedSourceAccount = accountService.findById(testAccount.getId()).orElseThrow();
        Account updatedTargetAccount = accountService.findById(targetAccount.getId()).orElseThrow();

        assertEquals(new BigDecimal("100.00"), updatedSourceAccount.getBalance());
        assertEquals(new BigDecimal("100.00"), updatedTargetAccount.getBalance());
    }

    @Test
    void whenFindByUser_thenAccountsAreReturned() {
        // Créer un deuxième compte
        accountService.createAccount(testUser, Account.AccountType.SAVINGS);

        List<Account> accounts = accountService.findByUser(testUser);

        assertEquals(2, accounts.size());
    }

    @Test
    void whenFreezeAccount_thenStatusIsUpdated() {
        accountService.freezeAccount(testAccount.getId());

        Account frozenAccount = accountService.findById(testAccount.getId()).orElseThrow();
        assertEquals("FROZEN", frozenAccount.getStatus());
    }

    @Test
    void whenUnfreezeAccount_thenStatusIsUpdated() {
        // D'abord geler le compte
        accountService.freezeAccount(testAccount.getId());

        // Ensuite dégeler
        accountService.unfreezeAccount(testAccount.getId());

        Account unfrozenAccount = accountService.findById(testAccount.getId()).orElseThrow();
        assertEquals("ACTIVE", unfrozenAccount.getStatus());
    }

    @Test
    void whenCloseAccount_thenStatusIsUpdated() {
        accountService.closeAccount(testAccount.getId());

        Account closedAccount = accountService.findById(testAccount.getId()).orElseThrow();
        assertEquals("CLOSED", closedAccount.getStatus());
    }
} 