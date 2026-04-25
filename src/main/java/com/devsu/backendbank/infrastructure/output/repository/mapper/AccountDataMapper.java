package com.devsu.backendbank.infrastructure.output.repository.mapper;

import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.infrastructure.output.repository.entity.Account;
import com.devsu.backendbank.infrastructure.output.repository.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountDataMapper {

    @Mapping(target = "clientId", source = "client.id")
    AccountDomain toDomain(Account entity);

    @Mapping(target = "client", source = "clientId")
    @Mapping(target = "transactions", ignore = true)
    Account toEntity(AccountDomain domain);

    default Client mapClient(Long clientId) {
        if (clientId == null) {
            return null;
        }
        Client client = new Client();
        client.setId(clientId);
        return client;
    }
}
