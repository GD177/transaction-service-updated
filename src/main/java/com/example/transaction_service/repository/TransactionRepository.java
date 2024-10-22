package com.example.transaction_service.repository;


import com.example.transaction_service.entity.Account;
import com.example.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);
    List<Transaction> findByAccountAndOperationTypeIdNot(Account account, int operationTypeId);
}
