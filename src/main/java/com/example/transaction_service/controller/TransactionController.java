package com.example.transaction_service.controller;

import com.example.transaction_service.dto.request.PayInstallmentRequestDTO;
import com.example.transaction_service.dto.request.TransactionRequestDTO;
import com.example.transaction_service.dto.response.PayInstallmentResponseDTO;
import com.example.transaction_service.dto.response.TransactionResponseDTO;
import com.example.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody final TransactionRequestDTO transactionDTO) {
        return new ResponseEntity<>(transactionService.createTransaction(transactionDTO), HttpStatus.CREATED);
    }

    @PostMapping("/transactions/installments/pay")
    public ResponseEntity<PayInstallmentResponseDTO> payInstallment(@Valid @RequestBody final PayInstallmentRequestDTO payInstallmentRequestDTO) {
        return new ResponseEntity<>(transactionService.payInstallmentByNumber(payInstallmentRequestDTO), HttpStatus.CREATED);
    }
}
