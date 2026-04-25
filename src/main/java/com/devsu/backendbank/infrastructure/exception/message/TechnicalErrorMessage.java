package com.devsu.backendbank.infrastructure.exception.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechnicalErrorMessage {

    INTERNAL_ERROR(
            "TBB0001",
            "An unexpected internal error occurred."
    ),

    DATABASE_ERROR(
            "TBB0002",
            "A database error occurred while processing the request."
    ),

    DATA_INTEGRITY_VIOLATION(
            "TBB0003",
            "A data integrity constraint was violated."
    ),

    TRANSACTION_FAILURE(
            "TBB0004",
            "The transaction failed or was rolled back."
    ),

    ENTITY_PERSISTENCE_ERROR(
            "TBB0005",
            "Failed to persist entity to the database."
    ),

    ENTITY_RETRIEVAL_ERROR(
            "TBB0006",
            "Failed to retrieve entity from the database."
    ),

    JSON_SERIALIZATION_ERROR(
            "TBB0007",
            "Error occurred while serializing response data."
    ),

    JSON_DESERIALIZATION_ERROR(
            "TBB0008",
            "Error occurred while parsing request body."
    ),

    INVALID_REQUEST_FORMAT(
            "TBB0009",
            "The request format is invalid or malformed."
    ),

    METHOD_ARGUMENT_NOT_VALID(
            "TBB0010",
            "Request validation failed due to invalid input data."
    ),

    NULL_POINTER(
            "TBB0011",
            "A null value was encountered unexpectedly."
    ),

    ILLEGAL_ARGUMENT(
            "TBB0012",
            "An illegal argument was provided."
    ),

    RESOURCE_LOCKED(
            "TBB0013",
            "The resource is currently locked or being modified."
    ),

    TIMEOUT_ERROR(
            "TBB0014",
            "The operation timed out."
    ),

    EXTERNAL_SERVICE_ERROR(
            "TBB0015",
            "An error occurred while calling an external service."
    ),

    CONFIGURATION_ERROR(
            "TBB0016",
            "Application configuration is invalid or missing."
    ),

    FILE_GENERATION_ERROR(
            "TBB0017",
            "Failed to generate report file."
    ),

    BASE64_ENCODING_ERROR(
            "TBB0018",
            "Failed to encode or decode Base64 data."
    ),

    SERVICE_NOT_FOUND(
            "TBB0019",
            "The requested service or endpoint was not found."
    ),

    METHOD_NOT_ALLOWED(
            "TBB0020",
            "HTTP method is not allowed for this endpoint."
    );

    private final String instance;
    private final String detail;

}
