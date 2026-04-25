package com.devsu.backendbank.infrastructure.output.repository;

import com.devsu.backendbank.infrastructure.output.repository.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Override
    @EntityGraph(attributePaths = {"person"})
    Optional<Client> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"person"})
    Page<Client> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"person"})
    Page<Client> findByEstado(Boolean estado, Pageable pageable);
}
