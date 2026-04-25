package com.devsu.backendbank.infrastructure.output.repository.mapper;

import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.infrastructure.output.repository.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersonDataMapper {

    PersonDomain toDomain(Person entity);

    @Mapping(target = "client", ignore = true)
    Person toEntity(PersonDomain domain);
}
