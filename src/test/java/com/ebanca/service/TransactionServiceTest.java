package com.ebanca.service;

import com.ebanca.model.Account;
import com.ebanca.model.Transaction;
import com.ebanca.model.User;
import com.ebanca.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account sourceAccount;
    private Account destinationAccount;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        sourceAccount = new Account();
        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("FR1234567890");
        sourceAccount.setBalance(BigDecimal.valueOf(1000.00));
        sourceAccount.setUser(user);

        destinationAccount = new Account();
        destinationAccount.setId(2L);
        destinationAccount.setAccountNumber("FR0987654321");
        destinationAccount.setBalance(BigDecimal.valueOf(500.00));
        destinationAccount.setUser(user);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setType(Transaction.TransactionType.TRANSFER);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
    }

    @Test
    void whenCreateTransaction_thenTransactionIsSaved() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Transaction result = transactionService.createTransaction(
            sourceAccount, 
            destinationAccount, 
            BigDecimal.valueOf(100.00), 
            "TRANSFER"
        );

        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        assertEquals(transaction.getAmount(), result.getAmount());
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void whenProcessTransaction_thenTransactionIsCompleted() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        transactionService.processTransaction(1L);

        verify(accountService).transfer(
            sourceAccount.getId(),
            destinationAccount.getId(),
            transaction.getAmount()
        );
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void whenProcessTransactionWithNonPendingStatus_thenThrowException() {
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(1L));
        verify(accountService, never()).transfer(any(), any(), any());
    }

    @Test
    void whenFindByAccount_thenTransactionsAreReturned() {
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(transactions);
        when(transactionRepository.findBySourceAccountOrTargetAccount(sourceAccount, sourceAccount, any()))
            .thenReturn(page);

        var result = transactionService.findByAccount(sourceAccount, PageRequest.of(0, 10)).getContent();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction.getId(), result.get(0).getId());
    }

    @Test
    void whenFindByStatus_thenTransactionsAreReturned() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findByStatus(Transaction.TransactionStatus.PENDING))
            .thenReturn(transactions);

        List<Transaction> result = transactionService.findByStatus(Transaction.TransactionStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(transaction.getId(), result.get(0).getId());
    }

    @Test
    void whenProcessTransactionFails_thenTransactionIsMarkedAsFailed() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        doThrow(new RuntimeException("Transfer failed"))
            .when(accountService).transfer(any(), any(), any());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        assertThrows(RuntimeException.class, () -> transactionService.processTransaction(1L));

        verify(transactionRepository).save(argThat(t -> 
            t.getStatus() == Transaction.TransactionStatus.FAILED &&
            t.getErrorMessage() != null
        ));
    }
} 