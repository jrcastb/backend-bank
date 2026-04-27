package com.devsu.backendbank.application.service.transaction;

import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;

import java.math.BigDecimal;

public record TransactionRuleContext(
        TransactionDomain transaction,
        AccountDomain account,
        BigDecimal currentBalance
) {
}

