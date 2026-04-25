package com.devsu.backendbank.infrastructure.input.dto.transaction;

import com.devsu.backendbank.domain.model.TransactionTypeDomain;

public enum TransactionTypeDto {
    CREDIT,
    DEBIT;

    public TransactionTypeDomain toDomain() {
        return switch (this) {
            case CREDIT -> TransactionTypeDomain.CREDITO;
            case DEBIT -> TransactionTypeDomain.DEBITO;
        };
    }

    public static TransactionTypeDto fromDomain(TransactionTypeDomain domain) {
        return switch (domain) {
            case CREDITO -> CREDIT;
            case DEBITO -> DEBIT;
        };
    }
}
