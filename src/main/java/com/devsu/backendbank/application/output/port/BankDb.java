package com.devsu.backendbank.application.output.port;

import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BankDb extends ClientQueryPort,
        ClientCommandPort,
        AccountQueryPort,
        AccountCommandPort,
        AccountLockPort,
        TransactionQueryPort,
        TransactionCommandPort,
        ReportQueryPort {
    Page<PersonDomain> findPersons(Pageable pageable);
    Optional<PersonDomain> findPersonById(Long id);
    Optional<PersonDomain> findPersonByIdentificacion(String identificacion);
    Page<AccountDomain> findAccountsByClient(Long clientId, Pageable pageable);
    Optional<AccountDomain> findAccountByNumber(String numeroCuenta);
    Page<TransactionDomain> findTransactionsByAccountAndDateRange(Long accountId,
                                                                  LocalDateTime fechaDesde,
                                                                  LocalDateTime fechaHasta,
                                                                  Pageable pageable);
    PersonDomain saveOrUpdatePerson(PersonDomain personDomain);
    Optional<AccountDomain> findAccountByNumberForUpdate(String numeroCuenta);
    BigDecimal sumDailyDebitsByClient(Long clientId,
                                      LocalDateTime fechaDesde,
                                      LocalDateTime fechaHasta);
}
