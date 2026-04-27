package com.devsu.backendbank.infrastructure.input.dto.account;

import com.devsu.backendbank.domain.enums.AccountTypeDomain;

public enum AccountTypeDto {
    AHORROS,
    CORRIENTE;

    public AccountTypeDomain toDomain() {
        return switch (this) {
            case AHORROS -> AccountTypeDomain.AHORROS;
            case CORRIENTE -> AccountTypeDomain.CORRIENTE;
        };
    }

    public static AccountTypeDto fromDomain(AccountTypeDomain domain) {
        return switch (domain) {
            case AHORROS -> AHORROS;
            case CORRIENTE -> CORRIENTE;
        };
    }
}
