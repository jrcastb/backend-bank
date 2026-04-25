package com.devsu.backendbank.infrastructure.output.repository.mapper;

import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.infrastructure.output.repository.entity.Account;
import com.devsu.backendbank.infrastructure.output.repository.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionDataMapper {

    @Mapping(target = "accountId", source = "account.id")
    TransactionDomain toDomain(Transaction entity);

    @Mapping(target = "account", source = "accountId")
    Transaction toEntity(TransactionDomain domain);

    default Account mapAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }
        Account account = new Account();
        account.setId(accountId);
        return account;
    }
}
