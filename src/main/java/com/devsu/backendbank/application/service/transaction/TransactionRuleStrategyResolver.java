package com.devsu.backendbank.application.service.transaction;

import com.devsu.backendbank.domain.enums.TransactionTypeDomain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TransactionRuleStrategyResolver {

    private final Map<TransactionTypeDomain, TransactionRuleStrategy> strategiesByType;

    public TransactionRuleStrategyResolver(List<TransactionRuleStrategy> strategies) {
        this.strategiesByType = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(TransactionRuleStrategy::supports, Function.identity()));
    }

    public TransactionRuleStrategy resolve(TransactionTypeDomain transactionType) {
        TransactionRuleStrategy strategy = strategiesByType.get(transactionType);
        if (strategy == null) {
            throw new IllegalArgumentException("No transaction rule strategy configured for type: " + transactionType);
        }
        return strategy;
    }
}

