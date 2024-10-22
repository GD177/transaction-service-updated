package com.example.transaction_service.unit.controller;

import com.example.transaction_service.controller.TransactionController;
import com.example.transaction_service.dto.request.PayInstallmentRequestDTO;
import com.example.transaction_service.dto.request.TransactionRequestDTO;
import com.example.transaction_service.dto.response.TransactionResponseDTO;
import com.example.transaction_service.exception.GlobalExceptionHandler;
import com.example.transaction_service.exception.InvalidRequestException;
import com.example.transaction_service.exception.ResourceNotFoundException;
import com.example.transaction_service.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest {
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private MockMvc mockMvc;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this); // Open mocks
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();  // Close mocks
    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        // Arrange
        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setTransactionId(1L);
        responseDTO.setMessage("Transaction created successfully");

        when(transactionService.createTransaction(any(TransactionRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("{\"accountId\": 1, \"amount\": 100, \"operationTypeId\": 1}"))
                .andExpect(status().is(CREATED.value()));

        verify(transactionService).createTransaction(any(TransactionRequestDTO.class)); // Verify service call
    }

    @Test
    void testCreateTransaction_Failure() throws Exception {
        // Arrange
        when(transactionService.createTransaction(any(TransactionRequestDTO.class)))
                .thenThrow(new InvalidRequestException("Invalid operation type ID: 9"));

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                        .contentType("application/json")
                        .content("{\"accountId\": 1, \"amount\": 100, \"operationTypeId\": 9}"))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request

        // Verify service call
        verify(transactionService).createTransaction(any(TransactionRequestDTO.class));
    }

    @Test
    void testPayInstallment_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/transactions/installments/pay")
                        .contentType("application/json")
                        .content("{\"transactionId\": 1, \"installmentNumber\": 1, \"accountId\": 1, \"amount\": 100}"))
                .andExpect(status().isCreated()); // Expect 200 OK

        // Verify service call
        verify(transactionService).payInstallmentByNumber(any(PayInstallmentRequestDTO.class));
    }

    @Test
    void testPayInstallment_Failure(){

        // Arrange
        PayInstallmentRequestDTO payInstallmentRequestDTO = new PayInstallmentRequestDTO();
        payInstallmentRequestDTO.setTransactionId(1L);
        payInstallmentRequestDTO.setInstallmentNumber(100);
        payInstallmentRequestDTO.setAccountId(19L);
        payInstallmentRequestDTO.setAmount(BigDecimal.valueOf(100));

        when(transactionService.payInstallmentByNumber(payInstallmentRequestDTO))
                .thenThrow(new ResourceNotFoundException("Installment not found"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                transactionController.payInstallment(payInstallmentRequestDTO));

        assertEquals("Installment not found", exception.getMessage());

        // Verify service call
        verify(transactionService).payInstallmentByNumber(any(PayInstallmentRequestDTO.class));
    }
}

