package com.ebanca.service;


import com.ebanca.exception.BusinessException;
import com.ebanca.exception.InsufficientFundsException;
import com.ebanca.exception.ResourceNotFoundException;
import com.ebanca.model.Account;
import com.ebanca.model.User;
import com.ebanca.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;
    private Account savedAccount;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        account = new Account();
        account.setAccountNumber("FR1234567890");
        account.setType(Account.AccountType.CHECKING);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        account.setUser(user);

        savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setAccountNumber(account.getAccountNumber());
        savedAccount.setType(account.getType());
        savedAccount.setBalance(account.getBalance());
        savedAccount.setStatus(account.getStatus());
        savedAccount.setUser(account.getUser());
    }

    @Test
    void whenCreateAccount_thenAccountIsSaved() {
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.existsByAccountNumber(account.getAccountNumber())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        Account result = accountService.createAccount(user, Account.AccountType.CHECKING);

        assertNotNull(result);
        assertEquals(savedAccount.getId(), result.getId());
        assertEquals(savedAccount.getAccountNumber(), result.getAccountNumber());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void whenCreateAccountWithExistingNumber_thenThrowException() {
        when(accountRepository.existsByAccountNumber(account.getAccountNumber())).thenReturn(true);

        assertThrows(BusinessException.class, () -> accountService.createAccount(user, Account.AccountType.CHECKING));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void whenDeposit_thenBalanceIsUpdated() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        BigDecimal amount = new BigDecimal("100.00");
        accountService.deposit(1L, amount);

        verify(accountRepository).save(argThat(a -> 
            a.getBalance().compareTo(amount) == 0
        ));
    }

    @Test
    void whenWithdrawWithSufficientFunds_thenBalanceIsUpdated() {
        savedAccount.setBalance(new BigDecimal("200.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        BigDecimal amount = new BigDecimal("100.00");
        accountService.withdraw(1L, amount);

        verify(accountRepository).save(argThat(a -> 
            a.getBalance().compareTo(new BigDecimal("100.00")) == 0
        ));
    }

    @Test
    void whenWithdrawWithInsufficientFunds_thenThrowException() {
        savedAccount.setBalance(new BigDecimal("50.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));

        BigDecimal amount = new BigDecimal("100.00");
        assertThrows(InsufficientFundsException.class, () -> accountService.withdraw(1L, amount));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void whenTransferWithValidAccounts_thenBalancesAreUpdated() {
        Account sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setBalance(new BigDecimal("200.00"));
        sourceAccount.setStatus("ACTIVE");

        Account targetAccount = new Account();
        targetAccount.setId(2L);
        targetAccount.setBalance(new BigDecimal("100.00"));
        targetAccount.setStatus("ACTIVE");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(sourceAccount, targetAccount);

        BigDecimal amount = new BigDecimal("50.00");
        accountService.transfer(1L, 2L, amount);

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void whenFindById_thenAccountIsReturned() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));

        Optional<Account> result = accountService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(savedAccount.getId(), result.get().getId());
        assertEquals(savedAccount.getAccountNumber(), result.get().getAccountNumber());
    }

    @Test
    void whenFindByIdWithInvalidId_thenThrowException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            accountService.findById(999L).orElseThrow(() -> new ResourceNotFoundException("Account not found")));
    }

    @Test
    void whenFindByUser_thenAccountsAreReturned() {
        List<Account> accounts = Arrays.asList(savedAccount);
        when(accountRepository.findByUser(user)).thenReturn(accounts);

        List<Account> result = accountService.findByUser(user);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(savedAccount.getId(), result.get(0).getId());
    }

    @Test
    void whenFreezeAccount_thenStatusIsUpdated() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        accountService.freezeAccount(1L);

        verify(accountRepository).save(argThat(a -> "FROZEN".equals(a.getStatus())));
    }

    @Test
    void whenUnfreezeAccount_thenStatusIsUpdated() {
        savedAccount.setStatus("FROZEN");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        accountService.unfreezeAccount(1L);

        verify(accountRepository).save(argThat(a -> "ACTIVE".equals(a.getStatus())));
    }

    @Test
    void whenCloseAccount_thenStatusIsUpdated() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savedAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        accountService.closeAccount(1L);

        verify(accountRepository).save(argThat(a -> "CLOSED".equals(a.getStatus())));
    }
} 