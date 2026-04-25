package com.devsu.backendbank.infrastructure.exception.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorMessage {

    CLIENT_NOT_FOUND(
            "BBB0001",
            "The specified client does not exist."
    ),

    ACCOUNT_NOT_FOUND(
            "BBB0002",
            "The specified account does not exist."
    ),

    ACCOUNT_INACTIVE(
            "BBB0003",
            "The account is inactive and cannot process transactions."
    ),

    CLIENT_INACTIVE(
            "BBB0004",
            "The client is inactive."
    ),

    INSUFFICIENT_FUNDS(
            "BBB0005",
            "Saldo no disponible"
    ),

    DAILY_LIMIT_EXCEEDED(
            "BBB0006",
            "Cupo diario Excedido"
    ),

    INVALID_TRANSACTION_TYPE(
            "BBB0007",
            "Invalid transaction type. Only CREDIT or DEBIT are allowed."
    ),

    INVALID_TRANSACTION_AMOUNT(
            "BBB0008",
            "Transaction amount must be a non-zero value."
    ),

    NEGATIVE_INITIAL_BALANCE(
            "BBB0009",
            "Initial balance cannot be negative."
    ),

    ACCOUNT_ALREADY_EXISTS(
            "BBB0010",
            "An account with the given number already exists."
    ),

    CLIENT_ALREADY_EXISTS(
            "BBB0011",
            "A client with the given identification already exists."
    ),

    INVALID_DATE_RANGE(
            "BBB0012",
            "Invalid date range. Start date must be before end date."
    ),

    NO_MOVEMENTS_FOUND(
            "BBB0013",
            "No movements found for the given criteria."
    ),

    ACCOUNT_OWNERSHIP_MISMATCH(
            "BBB0014",
            "The account does not belong to the specified client."
    ),

    INVALID_ACCOUNT_TYPE(
            "BBB0015",
            "Invalid account type."
    ),

    BAD_REQUEST_BODY(
            "BBB0016",
            "The request body is invalid or malformed."
    );

    private final String instance;
    private final String detail;
    
}
