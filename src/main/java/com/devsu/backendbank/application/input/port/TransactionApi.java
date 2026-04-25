package com.devsu.backendbank.application.input.port;

import com.devsu.backendbank.domain.model.TransactionDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionApi {

    Page<TransactionDomain> findTransactions(Pageable pageable);

    TransactionDomain findTransactionById(Long id);

    TransactionDomain createTransaction(TransactionDomain transaction);

    void deleteTransaction(Long id);
}
