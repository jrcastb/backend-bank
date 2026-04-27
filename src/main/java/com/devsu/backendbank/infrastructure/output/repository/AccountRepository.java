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

    @EntityGraph(attributePaths = {"client", "client.person"})
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"client", "client.person"})
    @Query("select a from Account a where a.numeroCuenta = :numeroCuenta")
    Optional<Account> findDetailedByNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    @EntityGraph(attributePaths = {"client", "client.person"})
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.numeroCuenta = :numeroCuenta")
    @EntityGraph(attributePaths = {"client", "client.person"})
    Optional<Account> findByNumeroCuentaForUpdate(@Param("numeroCuenta") String numeroCuenta);

    @EntityGraph(attributePaths = {"client", "client.person"})
    @Query("select a from Account a")
    Page<Account> findAllDetailed(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "client.person"})
    @Query("select a from Account a where a.client.id = :clientId")
    Page<Account> findByClientIdDetailed(@Param("clientId") Long clientId, Pageable pageable);
}
