package com.devsu.backendbank.infrastructure.output.adapter;

import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.infrastructure.exception.TechnicalException;
import com.devsu.backendbank.infrastructure.exception.message.TechnicalErrorMessage;
import com.devsu.backendbank.infrastructure.output.repository.AccountRepository;
import com.devsu.backendbank.infrastructure.output.repository.ClientRepository;
import com.devsu.backendbank.infrastructure.output.repository.PersonRepository;
import com.devsu.backendbank.infrastructure.output.repository.TransactionRepository;
import com.devsu.backendbank.infrastructure.output.repository.mapper.AccountDataMapper;
import com.devsu.backendbank.infrastructure.output.repository.mapper.ClientDataMapper;
import com.devsu.backendbank.infrastructure.output.repository.mapper.PersonDataMapper;
import com.devsu.backendbank.infrastructure.output.repository.mapper.TransactionDataMapper;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import com.devsu.backendbank.infrastructure.output.repository.entity.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class BankAdapterRepository implements BankDb {

    private final PersonRepository personRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PersonDataMapper personDataMapper;
    private final ClientDataMapper clientDataMapper;
    private final AccountDataMapper accountDataMapper;
    private final TransactionDataMapper transactionDataMapper;

    @Override
    public Page<PersonDomain> findPersons(Pageable pageable) {
        return personRepository.findAll(pageable)
                .map(personDataMapper::toDomain);
    }
    @Override
    public Optional<PersonDomain> findPersonById(Long id) {
        return personRepository.findById(id)
                .map(personDataMapper::toDomain);
    }
    @Override
    public Optional<PersonDomain> findPersonByIdentificacion(String identificacion) {
        return personRepository.findByIdentificacion(identificacion)
                .map(personDataMapper::toDomain);
    }
    @Override
    public Page<ClientDomain> findClients(Pageable pageable) {
        return clientRepository.findAll(pageable)
                .map(clientDataMapper::toDomain);
    }

    @Override
    public Optional<ClientDomain> findClientById(Long id) {
        return clientRepository.findById(id)
                .map(clientDataMapper::toDomain);
    }

    @Override
    public Page<AccountDomain> findAccounts(Pageable pageable) {
        return accountRepository.findAllDetailed(pageable)
                .map(accountDataMapper::toDomain);
    }

    @Override
    public Page<AccountDomain> findAccountsByClient(Long clientId, Pageable pageable) {
        return accountRepository.findByClientIdDetailed(clientId, pageable)
                .map(accountDataMapper::toDomain);
    }

    @Override
    public Optional<AccountDomain> findAccountById(Long id) {
        return accountRepository.findDetailedById(id)
                .map(accountDataMapper::toDomain);
    }

    @Override
    public Optional<AccountDomain> findAccountByNumber(String numeroCuenta) {
        return accountRepository.findDetailedByNumeroCuenta(numeroCuenta)
                .map(accountDataMapper::toDomain);
    }

    @Override
    public Page<TransactionDomain> findTransactions(Pageable pageable) {
        return transactionRepository.findAllDetailed(pageable)
                .map(transactionDataMapper::toDomain);
    }

    @Override
    public Optional<TransactionDomain> findTransactionById(Long id) {
        return transactionRepository.findDetailedById(id)
                .map(transactionDataMapper::toDomain);
    }

    @Override
    public Page<TransactionDomain> findTransactionsByAccountAndDateRange(Long accountId,
                                                                          LocalDateTime fechaDesde,
                                                                          LocalDateTime fechaHasta,
                                                                          Pageable pageable) {
        return transactionRepository.findDetailedByAccountIdAndFechaBetween(accountId, fechaDesde, fechaHasta, pageable)
                .map(transactionDataMapper::toDomain);
    }

    @Override
    public Optional<TransactionDomain> findLatestTransactionByAccountId(Long accountId) {
        return transactionRepository.findLatestDetailedByAccountId(accountId)
                .map(transactionDataMapper::toDomain);
    }

    @Override
    public Page<TransactionReportProjection> findReportByClientAndDateRange(Long clientId,
                                                                            LocalDateTime fechaDesde,
                                                                            LocalDateTime fechaHasta,
                                                                            Pageable pageable) {
        return transactionRepository.findReportByClientAndDateRange(clientId, fechaDesde, fechaHasta, pageable);
    }


    @Override
    public boolean personExistsByIdentificacion(String identificacion) {
        return personRepository.existsByIdentificacion(identificacion);
    }

    @Override
    public boolean personExistsByIdentificacionExcludingId(String identificacion, Long id) {
        return personRepository.existsByIdentificacionAndIdNot(identificacion, id);
    }

    @Override
    public boolean accountExistsByNumeroCuenta(String numeroCuenta) {
        return accountRepository.existsByNumeroCuenta(numeroCuenta);
    }

    @Override
    public boolean accountExistsByNumeroCuentaExcludingId(String numeroCuenta, Long id) {
        return accountRepository.existsByNumeroCuentaAndIdNot(numeroCuenta, id);
    }

    @Override
    public PersonDomain saveOrUpdatePerson(PersonDomain personDomain) {
        try {
            var entity = personDataMapper.toEntity(personDomain);
            var saved = personRepository.save(entity);
            return personDataMapper.toDomain(saved);
        } catch (Exception e) {
            log.error("Error al guardar/actualizar persona", e);
            throw new TechnicalException(e, TechnicalErrorMessage.ENTITY_PERSISTENCE_ERROR);
        }
    }

    @Override
    public ClientDomain saveOrUpdateClient(ClientDomain clientDomain) {
        try {
            var entity = clientDataMapper.toEntity(clientDomain);
            var savedPerson = personRepository.save(entity.getPerson());
            entity.setPerson(savedPerson);
            var saved = clientRepository.save(entity);
            saved.setPerson(savedPerson);
            return clientDataMapper.toDomain(saved);
        } catch (Exception e) {
            log.error("Error al guardar/actualizar cliente", e);
            throw new TechnicalException(e, TechnicalErrorMessage.ENTITY_PERSISTENCE_ERROR);
        }
    }

    @Override
    public AccountDomain saveOrUpdateAccount(AccountDomain accountDomain) {
        try {
            var entity = accountDataMapper.toEntity(accountDomain);
            var saved = accountRepository.save(entity);
            return accountRepository.findDetailedById(saved.getId())
                    .map(accountDataMapper::toDomain)
                    .orElseThrow(() -> new TechnicalException(TechnicalErrorMessage.ENTITY_PERSISTENCE_ERROR));
        } catch (Exception e) {
            log.error("Error al guardar/actualizar cuenta", e);
            throw new TechnicalException(e, TechnicalErrorMessage.ENTITY_PERSISTENCE_ERROR);
        }
    }

    @Override
    public TransactionDomain saveTransaction(TransactionDomain transactionDomain) {
        try {
            var entity = transactionDataMapper.toEntity(transactionDomain);
            var saved = transactionRepository.save(entity);
            return transactionRepository.findDetailedById(saved.getId())
                    .map(transactionDataMapper::toDomain)
                    .orElseThrow(() -> new TechnicalException(TechnicalErrorMessage.TRANSACTION_FAILURE));
        } catch (Exception e) {
            log.error("Error al registrar movimiento", e);
            throw new TechnicalException(e, TechnicalErrorMessage.TRANSACTION_FAILURE);
        }
    }

    @Override
    public void deleteClientById(Long id) {
        try {
            clientRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error al eliminar cliente {}", id, e);
            throw new TechnicalException(e, TechnicalErrorMessage.DATA_INTEGRITY_VIOLATION);
        }
    }

    @Override
    public void deleteAccountById(Long id) {
        try {
            accountRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error al eliminar cuenta {}", id, e);
            throw new TechnicalException(e, TechnicalErrorMessage.DATA_INTEGRITY_VIOLATION);
        }
    }

    @Override
    public void deleteTransactionById(Long id) {
        try {
            transactionRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error al eliminar movimiento {}", id, e);
            throw new TechnicalException(e, TechnicalErrorMessage.DATA_INTEGRITY_VIOLATION);
        }
    }

    @Override
    public Optional<AccountDomain> findAccountByIdForUpdate(Long accountId) {
        try {
            return accountRepository.findByIdForUpdate(accountId)
                    .map(accountDataMapper::toDomain);
        } catch (Exception e) {
            log.error("Error al adquirir lock pesimista en cuenta: {}", accountId, e);
            throw new TechnicalException(e, TechnicalErrorMessage.RESOURCE_LOCKED);
        }
    }

    @Override
    public Optional<AccountDomain> findAccountByNumberForUpdate(String numeroCuenta) {
        try {
            return accountRepository.findByNumeroCuentaForUpdate(numeroCuenta)
                    .map(accountDataMapper::toDomain);
        } catch (Exception e) {
            log.error("Error al adquirir lock pesimista en cuenta por número: {}", numeroCuenta, e);
            throw new TechnicalException(e, TechnicalErrorMessage.RESOURCE_LOCKED);
        }
    }

    @Override
    public BigDecimal sumDailyDebitsByAccount(Long accountId,
                                              LocalDateTime fechaDesde,
                                              LocalDateTime fechaHasta) {
        try {
            return transactionRepository.sumDailyDebitsByAccount(accountId, fechaDesde, fechaHasta, TransactionType.DEBITO);
        } catch (Exception e) {
            log.error("Error al calcular débitos diarios por cuenta: {}", accountId, e);
            throw new TechnicalException(e, TechnicalErrorMessage.DATABASE_ERROR);
        }
    }

    @Override
    public BigDecimal sumDailyDebitsByClient(Long clientId,
                                             LocalDateTime fechaDesde,
                                             LocalDateTime fechaHasta) {
        try {
            return transactionRepository.sumDailyDebitsByClient(clientId, fechaDesde, fechaHasta, TransactionType.DEBITO);
        } catch (Exception e) {
            log.error("Error al calcular débitos diarios por cliente: {}", clientId, e);
            throw new TechnicalException(e, TechnicalErrorMessage.DATABASE_ERROR);
        }
    }
}
