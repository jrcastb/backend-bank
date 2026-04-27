package com.devsu.backendbank.application.service.support;

import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.enums.AccountTypeDomain;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.domain.enums.GenderDomain;
import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.domain.enums.TransactionTypeDomain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ServiceTestFixtures {

    public static final Long CLIENT_ID = 1L;
    public static final Long ANOTHER_CLIENT_ID = 2L;
    public static final Long PERSON_ID = 3L;
    public static final Long ACCOUNT_ID = 10L;
    public static final Long TRANSACTION_ID = 100L;

    public static final String IDENTIFICACION = "0912345678";
    public static final String ANOTHER_IDENTIFICACION = "0999999999";
    public static final String CLIENTE_NOMBRE = "Jose Castillo";
    public static final String ANOTHER_CLIENTE_NOMBRE = "Jose Castillo Actualizado";
    public static final String NUMERO_CUENTA = "478758";
    public static final String ANOTHER_NUMERO_CUENTA = "225487";

    public static final LocalDate FECHA_DESDE = LocalDate.of(2026, 4, 1);
    public static final LocalDate FECHA_HASTA = LocalDate.of(2026, 4, 30);
    public static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 4, 20, 10, 30);
    public static final LocalDateTime UPDATED_AT = LocalDateTime.of(2026, 4, 21, 11, 45);

    public static final BigDecimal SALDO_INICIAL = new BigDecimal("2000.00");
    public static final BigDecimal DEBITO_300 = new BigDecimal("-300.00");
    public static final BigDecimal CREDITO_500 = new BigDecimal("500.00");
    public static final BigDecimal LIMITE_DIARIO = new BigDecimal("1000.00");

    private ServiceTestFixtures() {
    }

    public static PersonDomain currentPerson() {
        return new PersonDomain(
                PERSON_ID,
                CLIENTE_NOMBRE,
                GenderDomain.MASCULINO,
                29,
                IDENTIFICACION,
                "Av. Quito y Colon",
                "0999999999",
                CREATED_AT,
                UPDATED_AT
        );
    }

    public static PersonDomain incomingPerson() {
        return new PersonDomain(
                999L,
                ANOTHER_CLIENTE_NOMBRE,
                GenderDomain.MASCULINO,
                30,
                ANOTHER_IDENTIFICACION,
                "Av. Amazonas y Naciones Unidas",
                "0988888888",
                null,
                null
        );
    }

    public static ClientDomain currentClient() {
        return new ClientDomain(
                CLIENT_ID,
                currentPerson(),
                "Secret123",
                true,
                CREATED_AT,
                UPDATED_AT
        );
    }

    public static ClientDomain incomingClient() {
        return new ClientDomain(
                999L,
                incomingPerson(),
                "Secret999",
                false,
                null,
                null
        );
    }

    public static AccountDomain currentAccount() {
        return new AccountDomain(
                ACCOUNT_ID,
                CLIENT_ID,
                NUMERO_CUENTA,
                AccountTypeDomain.AHORROS,
                SALDO_INICIAL,
                true,
                CREATED_AT,
                UPDATED_AT,
                CLIENTE_NOMBRE
        );
    }

    public static AccountDomain inactiveAccount() {
        return new AccountDomain(
                ACCOUNT_ID,
                CLIENT_ID,
                NUMERO_CUENTA,
                AccountTypeDomain.AHORROS,
                SALDO_INICIAL,
                false,
                CREATED_AT,
                UPDATED_AT,
                CLIENTE_NOMBRE
        );
    }

    public static AccountDomain incomingAccount() {
        return new AccountDomain(
                999L,
                ANOTHER_CLIENT_ID,
                ANOTHER_NUMERO_CUENTA,
                AccountTypeDomain.CORRIENTE,
                new BigDecimal("1500.00"),
                false,
                null,
                null,
                ANOTHER_CLIENTE_NOMBRE
        );
    }

    public static TransactionDomain latestTransaction() {
        return new TransactionDomain(
                TRANSACTION_ID,
                ACCOUNT_ID,
                CREATED_AT,
                TransactionTypeDomain.CREDITO,
                CREDITO_500,
                new BigDecimal("1700.00"),
                CREATED_AT,
                NUMERO_CUENTA
        );
    }

    public static TransactionDomain creditTransactionInput() {
        return new TransactionDomain(
                null,
                ACCOUNT_ID,
                null,
                TransactionTypeDomain.CREDITO,
                CREDITO_500,
                null,
                null,
                NUMERO_CUENTA
        );
    }

    public static TransactionDomain debitTransactionInput() {
        return new TransactionDomain(
                null,
                ACCOUNT_ID,
                null,
                TransactionTypeDomain.DEBITO,
                DEBITO_300,
                null,
                null,
                NUMERO_CUENTA
        );
    }
}

