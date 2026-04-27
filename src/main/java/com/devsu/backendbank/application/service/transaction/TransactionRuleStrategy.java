package com.devsu.backendbank.application.service.transaction;

import com.devsu.backendbank.domain.enums.TransactionTypeDomain;

public interface TransactionRuleStrategy {
    TransactionTypeDomain supports();
    void validate(TransactionRuleContext context);
}

