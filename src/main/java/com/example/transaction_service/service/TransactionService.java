package com.example.transaction_service.service;

import com.example.transaction_service.dto.request.InstallmentRequestDTO;
import com.example.transaction_service.dto.request.PayInstallmentRequestDTO;
import com.example.transaction_service.dto.request.TransactionRequestDTO;
import com.example.transaction_service.dto.response.PayInstallmentResponseDTO;
import com.example.transaction_service.dto.response.TransactionResponseDTO;
import com.example.transaction_service.entity.Account;
import com.example.transaction_service.entity.Installment;
import com.example.transaction_service.entity.Transaction;
import com.example.transaction_service.enums.InstallmentStatus;
import com.example.transaction_service.enums.OperationType;
import com.example.transaction_service.exception.InvalidRequestException;
import com.example.transaction_service.exception.ResourceNotFoundException;
import com.example.transaction_service.repository.AccountRepository;
import com.example.transaction_service.repository.InstallmentRepository;
import com.example.transaction_service.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final InstallmentRepository installmentRepository;
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository,
                              InstallmentRepository installmentRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.installmentRepository = installmentRepository;
    }

    public TransactionResponseDTO createTransaction(final TransactionRequestDTO transactionDTO) {
        // Validate operation type ID
        OperationType operationType = validateOperationType(transactionDTO.getOperationTypeId());

        // Adjust the transaction amount based on the operation type
        // Convert to negative for purchases/withdrawals/purchase_with_installment
        BigDecimal adjustedAmount = adjustTransactionAmount(operationType, transactionDTO.getAmount());

        // Retrieve the account and handle if it does not exist
        Account account = findAccountById(transactionDTO.getAccountId());

        // Handle the actual transaction creation in a transactional method
        return handleTransactionCreationAndSave(account, operationType, adjustedAmount, transactionDTO);
    }

    @Transactional
    public TransactionResponseDTO handleTransactionCreationAndSave(Account account, OperationType operationType,
                                                            BigDecimal adjustedAmount, TransactionRequestDTO transactionDTO) {

        Transaction transaction = buildTransaction(account, operationType, adjustedAmount);

        if (operationType == OperationType.PURCHASE_INSTALLMENTS) {
            return getTransactionResponseDTO(createTransactionWithInstallments(transaction, transactionDTO));
        } else {
            Transaction savedTransaction = transactionRepository.save(transaction);
            logger.info("Created transaction with ID: {}", savedTransaction.getTransactionId());
            return getTransactionResponseDTO(savedTransaction);
        }
    }

    @Transactional
    public Transaction createTransactionWithInstallments(Transaction transaction, TransactionRequestDTO transactionDTO) {

        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("inside createTransactionWithInstallments transaction id : {}", savedTransaction.getTransactionId());
        // Create individual installment records and link them to the transaction
        int installmentNumber = 1;
        for (InstallmentRequestDTO installments : transactionDTO.getInstallments()) {
            Installment installment = new Installment();
            installment.setTransaction(savedTransaction);
            installment.setInstallmentNumber(installmentNumber);
            installment.setInstallmentAmount(installments.getAmount());
            installment.setStatus(InstallmentStatus.PENDING);
            installmentRepository.save(installment);

            installmentNumber++;
        }

        return savedTransaction;
    }

    @Transactional
    public PayInstallmentResponseDTO payInstallmentByNumber(final PayInstallmentRequestDTO payInstallmentRequest) {
        // Step 1: Find the transaction and its corresponding installment
        Transaction transaction = transactionRepository.findById(payInstallmentRequest.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Installment installment = installmentRepository.findByTransactionAndInstallmentNumber(transaction,
                        payInstallmentRequest.getInstallmentNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Installment not found"));

        // Step 2: Check if the installment is already paid
        if (installment.getStatus() == InstallmentStatus.PAID) {
            throw new InvalidRequestException("Installment is already paid");
        }

        // Step 3: Check if the amount matches the installment amount
        if (installment.getInstallmentAmount().compareTo(payInstallmentRequest.getAmount()) != 0) {
            throw new InvalidRequestException("Paid amount does not match the installment amount");
        }

        // Step 4: Create a new transaction for the installment payment
        Transaction installmentPaymentTransaction = new Transaction();
        installmentPaymentTransaction.setAccount(findAccountById(payInstallmentRequest.getAccountId()));
        installmentPaymentTransaction.setOperationTypeId(OperationType.INSTALLMENT_PAYMENT.getId());
        installmentPaymentTransaction.setAmount(payInstallmentRequest.getAmount());
        transactionRepository.save(installmentPaymentTransaction);

        // Step 5: Mark the installment as paid
        installment.setStatus(InstallmentStatus.PAID);
        installmentRepository.save(installment);

        return buildPayInstallmentResponse(installmentPaymentTransaction);
    }




    // HELPER METHODS
    private TransactionResponseDTO getTransactionResponseDTO(Transaction savedTransaction) {
        // Map to TransactionResponseDTO
        TransactionResponseDTO responseDTO = new TransactionResponseDTO();
        responseDTO.setTransactionId(savedTransaction.getTransactionId());
        responseDTO.setMessage("Transaction created successfully");
        return responseDTO;
    }

    private OperationType validateOperationType(int operationTypeId) {
        try {
            return OperationType.fromId(operationTypeId);
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid operation type ID: {}", operationTypeId);
            throw new InvalidRequestException(ex.getMessage());
        }
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    logger.error("Account not found with ID: {}", accountId);
                    return new ResourceNotFoundException("Account not found with ID: " + accountId);
                });
    }

    private BigDecimal adjustTransactionAmount(OperationType operationType, BigDecimal amount) {
        if (operationType == OperationType.NORMAL_PURCHASE || operationType == OperationType.WITHDRAWAL ||
                operationType == OperationType.PURCHASE_INSTALLMENTS) {
            return amount.negate();
        }
        return amount;
    }

    private Transaction buildTransaction(Account account, OperationType operationType, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setOperationTypeId(operationType.getId());
        transaction.setAmount(amount);
        return transaction;
    }

    private PayInstallmentResponseDTO buildPayInstallmentResponse(Transaction savedTransaction)
    {
        PayInstallmentResponseDTO response = new PayInstallmentResponseDTO();
        response.setTransactionId(savedTransaction.getTransactionId());
        response.setMessage("Installment paid successfully");
        response.setSuccess(true);
        return response;
    }
}
