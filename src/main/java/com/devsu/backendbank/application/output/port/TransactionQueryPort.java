package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.TransactionDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionQueryPort {
    Page<TransactionDomain> findTransactions(Pageable pageable);
    Optional<TransactionDomain> findTransactionById(Long id);
    Optional<TransactionDomain> findLatestTransactionByAccountId(Long accountId);
    BigDecimal sumDailyDebitsByAccount(Long accountId, LocalDateTime fechaDesde, LocalDateTime fechaHasta);
}

