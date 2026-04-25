package com.devsu.backendbank.infrastructure.output.repository.mapper;

import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.infrastructure.output.repository.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = PersonDataMapper.class)
public interface ClientDataMapper {

    ClientDomain toDomain(Client entity);

    @Mapping(target = "accounts", ignore = true)
    Client toEntity(ClientDomain domain);
}
