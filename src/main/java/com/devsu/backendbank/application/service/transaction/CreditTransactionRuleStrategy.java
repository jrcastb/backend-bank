package com.devsu.backendbank.application.service.transaction;

import com.devsu.backendbank.domain.enums.TransactionTypeDomain;
import org.springframework.stereotype.Component;

@Component
public class CreditTransactionRuleStrategy implements TransactionRuleStrategy {

    @Override
    public TransactionTypeDomain supports() {
        return TransactionTypeDomain.CREDITO;
    }

    @Override
    public void validate(TransactionRuleContext context) {
        // No reglas adicionales para créditos actualmente.
    }
}

