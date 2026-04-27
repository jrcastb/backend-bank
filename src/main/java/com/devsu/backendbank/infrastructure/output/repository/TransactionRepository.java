package com.devsu.backendbank.infrastructure.output.repository;

import com.devsu.backendbank.infrastructure.output.repository.entity.Transaction;
import com.devsu.backendbank.infrastructure.output.repository.entity.TransactionType;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @EntityGraph(attributePaths = {"account"})
    @Query("select t from Transaction t")
    Page<Transaction> findAllDetailed(Pageable pageable);

    @EntityGraph(attributePaths = {"account"})
    @Query("select t from Transaction t where t.id = :id")
    Optional<Transaction> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"account"})
    Optional<Transaction> findFirstByAccount_IdOrderByFechaDescIdDesc(Long accountId);

    @EntityGraph(attributePaths = {"account"})
    @Query("select t from Transaction t where t.account.id = :accountId and t.fecha between :fechaDesde and :fechaHasta")
    Page<Transaction> findDetailedByAccountIdAndFechaBetween(@Param("accountId") Long accountId,
                                                             @Param("fechaDesde") LocalDateTime fechaDesde,
                                                             @Param("fechaHasta") LocalDateTime fechaHasta,
                                                             Pageable pageable);

    @Query("""
            select coalesce(sum(abs(t.valor)), 0)
            from Transaction t
            where t.account.id = :accountId
              and t.tipoMovimiento = :debitType
              and t.fecha between :fechaDesde and :fechaHasta
            """)
    BigDecimal sumDailyDebitsByAccount(@Param("accountId") Long accountId,
                                       @Param("fechaDesde") LocalDateTime fechaDesde,
                                       @Param("fechaHasta") LocalDateTime fechaHasta,
                                       @Param("debitType") TransactionType debitType);

    @Query("""
            select coalesce(sum(abs(t.valor)), 0)
            from Transaction t
            join t.account a
            join a.client c
            where c.id = :clientId
              and t.tipoMovimiento = :debitType
              and t.fecha between :fechaDesde and :fechaHasta
            """)
    BigDecimal sumDailyDebitsByClient(@Param("clientId") Long clientId,
                                      @Param("fechaDesde") LocalDateTime fechaDesde,
                                      @Param("fechaHasta") LocalDateTime fechaHasta,
                                      @Param("debitType") TransactionType debitType);

    @Query(value = """
            select t.fecha as fecha,
                   p.nombre as cliente,
                   a.numeroCuenta as numeroCuenta,
                   a.tipoCuenta as tipoCuenta,
                   a.saldoInicial as saldoInicial,
                   a.estado as estadoCuenta,
                   t.valor as movimiento,
                   t.saldo as saldoDisponible
            from Transaction t
            join t.account a
            join a.client c
            join c.person p
            where c.id = :clientId
              and t.fecha between :fechaDesde and :fechaHasta
            order by t.fecha asc, t.id asc
            """,
            countQuery = """
            select count(t.id)
            from Transaction t
            join t.account a
            join a.client c
            where c.id = :clientId
              and t.fecha between :fechaDesde and :fechaHasta
            """
    )
    Page<TransactionReportProjection> findReportByClientAndDateRange(@Param("clientId") Long clientId,
                                                                     @Param("fechaDesde") LocalDateTime fechaDesde,
                                                                     @Param("fechaHasta") LocalDateTime fechaHasta,
                                                                     Pageable pageable);
}
