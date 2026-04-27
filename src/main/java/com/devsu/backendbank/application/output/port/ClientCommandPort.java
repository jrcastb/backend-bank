package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.ClientDomain;

public interface ClientCommandPort {
    boolean personExistsByIdentificacion(String identificacion);
    boolean personExistsByIdentificacionExcludingId(String identificacion, Long id);
    ClientDomain saveOrUpdateClient(ClientDomain clientDomain);
    void deleteClientById(Long id);
}

