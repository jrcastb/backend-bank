package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.ClientDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ClientQueryPort {
    Page<ClientDomain> findClients(Pageable pageable);
    Optional<ClientDomain> findClientById(Long id);
}

