package com.devsu.backendbank.application.input.port;

import com.devsu.backendbank.domain.model.ClientDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientApi {

    Page<ClientDomain> findClients(Pageable pageable);

    ClientDomain findClientById(Long id);

    ClientDomain createClient(ClientDomain client);

    ClientDomain updateClient(Long id, ClientDomain client);

    ClientDomain patchClient(Long id, ClientDomain client);

    void deleteClient(Long id);
}
