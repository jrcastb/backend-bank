package com.devsu.backendbank.infrastructure.output.adapter;

import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.domain.model.PersonDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.infrastructure.output.repository.AccountRepository;
import com.devsu.backendbank.infrastructure.output.repository.ClientRepository;
import com.devsu.backendbank.infrastructure.output.repository.PersonRepository;
import com.devsu.backendbank.infrastructure.output.repository.TransactionRepository;
import com.devsu.backendbank.infrastructure.output.repository.mapper.AccountDataMapper;
import com.devsu.backendbank.infrastructure.output.repository.mapper.ClientDataMapper;
import com.devsu.backendbank.infrastructure.output.repository.mapper.PersonDataMapper;
import com.devsu.backendbank.infrastructure.output.repository.mapper.TransactionDataMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class BankAdapterRepository {

    private final PersonRepository personRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PersonDataMapper personDataMapper;
    private final ClientDataMapper clientDataMapper;
    private final AccountDataMapper accountDataMapper;
    private final TransactionDataMapper transactionDataMapper;

    public BankAdapterRepository(PersonRepository personRepository,
                                 ClientRepository clientRepository,
                                 AccountRepository accountRepository,
                                 TransactionRepository transactionRepository,
                                 PersonDataMapper personDataMapper,
                                 ClientDataMapper clientDataMapper,
                                 AccountDataMapper accountDataMapper,
                                 TransactionDataMapper transactionDataMapper) {
        this.personRepository = personRepository;
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.personDataMapper = personDataMapper;
        this.clientDataMapper = clientDataMapper;
        this.accountDataMapper = accountDataMapper;
        this.transactionDataMapper = transactionDataMapper;
    }

    public Optional<PersonDomain> findPersonById(Long id) {
        return personRepository.findById(id)
                .map(personDataMapper::toDomain);
    }

    public Optional<PersonDomain> findPersonByIdentificacion(String identificacion) {
        return personRepository.findByIdentificacion(identificacion)
                .map(personDataMapper::toDomain);
    }

    public Page<ClientDomain> findClients(Pageable pageable) {
        return clientRepository.findAll(pageable)
                .map(clientDataMapper::toDomain);
    }

    public Optional<ClientDomain> findClientById(Long id) {
        return clientRepository.findById(id)
                .map(clientDataMapper::toDomain);
    }

    public Page<AccountDomain> findAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(accountDataMapper::toDomain);
    }

    public Optional<AccountDomain> findAccountById(Long id) {
        return accountRepository.findById(id)
                .map(accountDataMapper::toDomain);
    }

    public Optional<AccountDomain> findAccountByNumber(String numeroCuenta) {
        return accountRepository.findByNumeroCuenta(numeroCuenta)
                .map(accountDataMapper::toDomain);
    }

    public Page<TransactionDomain> findTransactionsByAccountAndDateRange(Long accountId,
                                                                          LocalDateTime fechaDesde,
                                                                          LocalDateTime fechaHasta,
                                                                          Pageable pageable) {
        return transactionRepository.findByAccount_IdAndFechaBetween(accountId, fechaDesde, fechaHasta, pageable)
                .map(transactionDataMapper::toDomain);
    }
}
