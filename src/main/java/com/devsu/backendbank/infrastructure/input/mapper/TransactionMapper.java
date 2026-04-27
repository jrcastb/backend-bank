package com.devsu.backendbank.infrastructure.input.mapper;

import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.domain.enums.TransactionTypeDomain;
import com.devsu.backendbank.infrastructure.input.dto.transaction.TransactionCreateRequest;
import com.devsu.backendbank.infrastructure.input.dto.transaction.TransactionResponse;
import com.devsu.backendbank.infrastructure.input.dto.transaction.TransactionTypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountId", source = "cuentaId")
    @Mapping(target = "tipoMovimiento", source = "tipoMovimiento")
    @Mapping(target = "valor", expression = "java(normalizeAmount(request.valor(), request.tipoMovimiento()))")
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "saldo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "numeroCuenta", ignore = true)
    TransactionDomain toDomain(TransactionCreateRequest request);

    @Mapping(target = "numeroCuenta", source = "numeroCuenta")
    @Mapping(target = "tipoMovimiento", source = "tipoMovimiento")
    TransactionResponse toResponse(TransactionDomain domain);

    default TransactionTypeDomain map(TransactionTypeDto source) {
        return source != null ? source.toDomain() : null;
    }

    default TransactionTypeDto map(TransactionTypeDomain source) {
        return source != null ? TransactionTypeDto.fromDomain(source) : null;
    }

    default BigDecimal normalizeAmount(BigDecimal raw, TransactionTypeDto type) {
        if (raw == null || type == null) {
            return raw;
        }
        return switch (type) {
            case CREDIT -> raw.abs();
            case DEBIT -> raw.abs().negate();
        };
    }
}
