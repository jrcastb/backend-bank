package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.input.port.ClientApi;
import com.devsu.backendbank.application.output.port.ClientCommandPort;
import com.devsu.backendbank.application.output.port.ClientQueryPort;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService implements ClientApi {

    private final ClientQueryPort clientQueryPort;
    private final ClientCommandPort clientCommandPort;

    @Override
    public Page<ClientDomain> findClients(Pageable pageable) {
        return clientQueryPort.findClients(pageable);
    }

    @Override
    public ClientDomain findClientById(Long id) {
        return clientQueryPort.findClientById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorMessage.CLIENT_NOT_FOUND));
    }

    @Override
    public ClientDomain createClient(ClientDomain client) {
        if (clientCommandPort.personExistsByIdentificacion(client.person().identificacion())) {
            throw new BusinessException(BusinessErrorMessage.CLIENT_ALREADY_EXISTS);
        }
        return clientCommandPort.saveOrUpdateClient(client);
    }

    @Override
    public ClientDomain updateClient(Long id, ClientDomain client) {
        ClientDomain current = findClientById(id);
        Long personId = current.person().id();
        if (personId != null && clientCommandPort.personExistsByIdentificacionExcludingId(client.person().identificacion(), personId)) {
            throw new BusinessException(BusinessErrorMessage.CLIENT_ALREADY_EXISTS);
        }
        return clientCommandPort.saveOrUpdateClient(preserveIdentity(current, client));
    }

    @Override
    public ClientDomain patchClient(Long id, ClientDomain client) {
        ClientDomain current = findClientById(id);
        Long personId = current.person().id();
        if (personId != null && clientCommandPort.personExistsByIdentificacionExcludingId(client.person().identificacion(), personId)) {
            throw new BusinessException(BusinessErrorMessage.CLIENT_ALREADY_EXISTS);
        }
        return clientCommandPort.saveOrUpdateClient(preserveIdentity(current, client));
    }

    @Override
    public void deleteClient(Long id) {
        findClientById(id);
        clientCommandPort.deleteClientById(id);
    }

    private ClientDomain preserveIdentity(ClientDomain current, ClientDomain incoming) {
        PersonDomain currentPerson = current.person();
        PersonDomain inPerson = incoming.person();
        PersonDomain person = new PersonDomain(
                currentPerson.id(),
                inPerson.nombre(),
                inPerson.genero(),
                inPerson.edad(),
                inPerson.identificacion(),
                inPerson.direccion(),
                inPerson.telefono(),
                currentPerson.createdAt(),
                currentPerson.updatedAt()
        );

        return new ClientDomain(
                current.id(),
                person,
                incoming.contrasena(),
                incoming.estado(),
                current.createdAt(),
                current.updatedAt()
        );
    }
}
