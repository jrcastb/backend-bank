package com.devsu.backendbank.infrastructure.output.repository;

import com.devsu.backendbank.infrastructure.output.repository.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByIdentificacion(String identificacion);

    boolean existsByIdentificacion(String identificacion);

    boolean existsByIdentificacionAndIdNot(String identificacion, Long id);
}
