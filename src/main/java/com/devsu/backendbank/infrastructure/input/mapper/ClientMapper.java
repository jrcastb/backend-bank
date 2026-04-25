package com.devsu.backendbank.infrastructure.input.mapper;

import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.domain.model.GenderDomain;
import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientCreateRequest;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientPatchRequest;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientResponse;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientUpdateRequest;
import com.devsu.backendbank.infrastructure.input.dto.common.GenderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person", expression = "java(toPerson(request))")
    @Mapping(target = "estado", expression = "java(request.estado() == null || request.estado())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ClientDomain toDomain(ClientCreateRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "person", expression = "java(toPerson(request))")
    @Mapping(target = "estado", expression = "java(request.estado() == null || request.estado())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ClientDomain toDomain(Long id, ClientUpdateRequest request);

    default ClientDomain merge(ClientDomain current, ClientPatchRequest patch) {
        PersonDomain person = current.person();
        PersonDomain mergedPerson = new PersonDomain(
                person.id(),
                patch.nombre() != null ? patch.nombre() : person.nombre(),
                patch.genero() != null ? patch.genero().toDomain() : person.genero(),
                patch.edad() != null ? patch.edad() : person.edad(),
                patch.identificacion() != null ? patch.identificacion() : person.identificacion(),
                patch.direccion() != null ? patch.direccion() : person.direccion(),
                patch.telefono() != null ? patch.telefono() : person.telefono(),
                person.createdAt(),
                person.updatedAt()
        );

        return new ClientDomain(
                current.id(),
                mergedPerson,
                patch.contrasena() != null ? patch.contrasena() : current.contrasena(),
                patch.estado() != null ? patch.estado() : current.estado(),
                current.createdAt(),
                current.updatedAt()
        );
    }

    @Mapping(target = "nombre", source = "person.nombre")
    @Mapping(target = "genero", source = "person.genero")
    @Mapping(target = "edad", source = "person.edad")
    @Mapping(target = "identificacion", source = "person.identificacion")
    @Mapping(target = "direccion", source = "person.direccion")
    @Mapping(target = "telefono", source = "person.telefono")
    ClientResponse toResponse(ClientDomain domain);

    default PersonDomain toPerson(ClientCreateRequest request) {
        return new PersonDomain(
                null,
                request.nombre(),
                request.genero().toDomain(),
                request.edad(),
                request.identificacion(),
                request.direccion(),
                request.telefono(),
                null,
                null
        );
    }

    default PersonDomain toPerson(ClientUpdateRequest request) {
        return new PersonDomain(
                null,
                request.nombre(),
                request.genero().toDomain(),
                request.edad(),
                request.identificacion(),
                request.direccion(),
                request.telefono(),
                null,
                null
        );
    }

    default GenderDto map(GenderDomain source) {
        return source != null ? GenderDto.fromDomain(source) : null;
    }
}
