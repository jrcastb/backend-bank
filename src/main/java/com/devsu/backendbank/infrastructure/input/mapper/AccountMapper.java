package com.devsu.backendbank.infrastructure.input.mapper;

import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.AccountTypeDomain;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountCreateRequest;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountPatchRequest;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountResponse;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountTypeDto;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientId", source = "clienteId")
    @Mapping(target = "tipoCuenta", source = "tipoCuenta")
    @Mapping(target = "estado", expression = "java(request.estado() == null || request.estado())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccountDomain toDomain(AccountCreateRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "clientId", source = "request.clienteId")
    @Mapping(target = "tipoCuenta", source = "request.tipoCuenta")
    @Mapping(target = "estado", expression = "java(request.estado() == null || request.estado())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AccountDomain toDomain(Long id, AccountUpdateRequest request);

    default AccountDomain merge(AccountDomain current, AccountPatchRequest patch) {
        return new AccountDomain(
                current.id(),
                current.clientId(),
                patch.numeroCuenta() != null ? patch.numeroCuenta() : current.numeroCuenta(),
                patch.tipoCuenta() != null ? patch.tipoCuenta().toDomain() : current.tipoCuenta(),
                patch.saldoInicial() != null ? patch.saldoInicial() : current.saldoInicial(),
                patch.estado() != null ? patch.estado() : current.estado(),
                current.createdAt(),
                current.updatedAt()
        );
    }

    @Mapping(target = "clienteId", source = "clientId")
    @Mapping(target = "tipoCuenta", source = "tipoCuenta")
    AccountResponse toResponse(AccountDomain domain);

    default AccountTypeDomain map(AccountTypeDto source) {
        return source != null ? source.toDomain() : null;
    }

    default AccountTypeDto map(AccountTypeDomain source) {
        return source != null ? AccountTypeDto.fromDomain(source) : null;
    }
}
