package com.devsu.backendbank.infrastructure.output.repository;

import com.devsu.backendbank.infrastructure.output.repository.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuentaAndIdNot(String numeroCuenta, Long id);

    Optional<Account> findByNumeroCuenta(String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.numeroCuenta = :numeroCuenta")
    Optional<Account> findByNumeroCuentaForUpdate(@Param("numeroCuenta") String numeroCuenta);

    @EntityGraph(attributePaths = {"client", "client.person"})
    Page<Account> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "client.person"})
    Page<Account> findByClientId(Long clientId, Pageable pageable);
}
