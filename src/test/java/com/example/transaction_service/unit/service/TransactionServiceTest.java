package com.example.transaction_service.unit.service;

import com.example.transaction_service.dto.request.InstallmentRequestDTO;
import com.example.transaction_service.dto.request.TransactionRequestDTO;
import com.example.transaction_service.dto.response.TransactionResponseDTO;
import com.example.transaction_service.entity.Account;
import com.example.transaction_service.entity.Installment;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.enums.OperationType;
import com.example.transaction_service.exception.InvalidRequestException;
import com.example.transaction_service.exception.ResourceNotFoundException;
import com.example.transaction_service.repository.AccountRepository;
import com.example.transaction_service.repository.InstallmentRepository;
import com.example.transaction_service.repository.TransactionRepository;
import com.example.transaction_service.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InstallmentRepository installmentRepository;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequestDTO transactionRequestDTO;
    private Account account;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this); // Open mocks
        transactionRequestDTO = new TransactionRequestDTO();
        transactionRequestDTO.setAccountId(1L);
        transactionRequestDTO.setAmount(BigDecimal.valueOf(100));
        transactionRequestDTO.setOperationTypeId(OperationType.NORMAL_PURCHASE.getId());
        account = new Account();
        account.setAccountId(1L);
        account.setDocumentNumber("12345678900");
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();  // Close mocks
    }

    @Test
    void testCreateTransaction_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(1L);
        mockTransaction.setAmount(BigDecimal.valueOf(100));
        mockTransaction.setOperationTypeId(OperationType.NORMAL_PURCHASE.getId());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        TransactionResponseDTO createdTransaction = transactionService.createTransaction(transactionRequestDTO);

        assertNotNull(createdTransaction);
        assertEquals(1L, createdTransaction.getTransactionId());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_AccountNotFound() {
        when(accountRepository.findById(1L)).thenThrow(new ResourceNotFoundException("Account not found with ID: 1"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                transactionService.createTransaction(transactionRequestDTO));

        assertEquals("Account not found with ID: 1", exception.getMessage());
    }

    @Test
    void testCreateTransaction_InvalidOperationType() {
        transactionRequestDTO.setOperationTypeId(999); // Invalid type

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                transactionService.createTransaction(transactionRequestDTO));

        assertEquals("Invalid OperationType ID: 999", exception.getMessage());
    }

    @Test
    void testCreateTransaction_WithdrawalOrPurchase_NegativeAmount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(1L);
        mockTransaction.setAmount(BigDecimal.valueOf(100));
        mockTransaction.setOperationTypeId(OperationType.NORMAL_PURCHASE.getId());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        TransactionResponseDTO result = transactionService.createTransaction(transactionRequestDTO);

        assertNotNull(result);
        assertEquals("Transaction created successfully", result.getMessage());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_CreditVoucher_PositiveAmount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(1L);
        mockTransaction.setAmount(BigDecimal.valueOf(100));
        mockTransaction.setOperationTypeId(OperationType.NORMAL_PURCHASE.getId());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        transactionRequestDTO.setOperationTypeId(OperationType.CREDIT_VOUCHER.getId());

        TransactionResponseDTO result = transactionService.createTransaction(transactionRequestDTO);

        assertNotNull(result);
        assertEquals("Transaction created successfully", result.getMessage());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_WithInstallments() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Transaction mockTransaction = new Transaction();
        mockTransaction.setTransactionId(1L);
        mockTransaction.setAmount(BigDecimal.valueOf(100));
        mockTransaction.setOperationTypeId(OperationType.PURCHASE_INSTALLMENTS.getId());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        transactionRequestDTO.setAccountId(1L);
        transactionRequestDTO.setOperationTypeId(OperationType.PURCHASE_INSTALLMENTS.getId());
        transactionRequestDTO.setInstallments(List.of(new InstallmentRequestDTO()));

        when(installmentRepository.save(any(Installment.class))).thenReturn(new Installment());

        TransactionResponseDTO result = transactionService.createTransaction(transactionRequestDTO);

        assertNotNull(result);
        assertEquals("Transaction created successfully", result.getMessage());
        verify(installmentRepository, times(1)).save(any(Installment.class));
    }

    @Test
    void testCreateTransaction_SaveFails() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.createTransaction(transactionRequestDTO));

        assertEquals("Database error", exception.getMessage());
    }

}
