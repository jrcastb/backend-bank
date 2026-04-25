package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BankDb {
    Page<PersonDomain> findPersons(Pageable pageable);
    Optional<PersonDomain> findPersonById(Long id);
    Optional<PersonDomain> findPersonByIdentificacion(String identificacion);
    Page<ClientDomain> findClients(Pageable pageable);
    Optional<ClientDomain> findClientById(Long id);
    Page<AccountDomain> findAccounts(Pageable pageable);
    Page<AccountDomain> findAccountsByClient(Long clientId, Pageable pageable);
    Optional<AccountDomain> findAccountById(Long id);
    Optional<AccountDomain> findAccountByNumber(String numeroCuenta);
    Page<TransactionDomain> findTransactions(Pageable pageable);
    Optional<TransactionDomain> findTransactionById(Long id);
    Page<TransactionDomain> findTransactionsByAccountAndDateRange(Long accountId,
                                                                  LocalDateTime fechaDesde,
                                                                  LocalDateTime fechaHasta,
                                                                  Pageable pageable);
    Optional<TransactionDomain> findLatestTransactionByAccountId(Long accountId);
    Page<TransactionReportProjection> findReportByClientAndDateRange(Long clientId,
                                                                     LocalDateTime fechaDesde,
                                                                     LocalDateTime fechaHasta,
                                                                     Pageable pageable);
    boolean personExistsByIdentificacion(String identificacion);
    boolean personExistsByIdentificacionExcludingId(String identificacion, Long id);
    boolean accountExistsByNumeroCuenta(String numeroCuenta);
    boolean accountExistsByNumeroCuentaExcludingId(String numeroCuenta, Long id);
    PersonDomain saveOrUpdatePerson(PersonDomain personDomain);
    ClientDomain saveOrUpdateClient(ClientDomain clientDomain);
    AccountDomain saveOrUpdateAccount(AccountDomain accountDomain);
    TransactionDomain saveTransaction(TransactionDomain transactionDomain);
    void deleteClientById(Long id);
    void deleteAccountById(Long id);
    void deleteTransactionById(Long id);
    Optional<AccountDomain> findAccountByIdForUpdate(Long accountId);
    Optional<AccountDomain> findAccountByNumberForUpdate(String numeroCuenta);
    BigDecimal sumDailyDebitsByAccount(Long accountId,
                                       LocalDateTime fechaDesde,
                                       LocalDateTime fechaHasta);
    BigDecimal sumDailyDebitsByClient(Long clientId,
                                      LocalDateTime fechaDesde,
                                      LocalDateTime fechaHasta);
}
