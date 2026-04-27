package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.TransactionDomain;

public interface TransactionCommandPort {
    TransactionDomain saveTransaction(TransactionDomain transactionDomain);
    void deleteTransactionById(Long id);
}

