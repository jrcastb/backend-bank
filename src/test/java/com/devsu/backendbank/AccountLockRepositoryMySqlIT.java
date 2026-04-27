package com.devsu.backendbank;

import com.devsu.backendbank.infrastructure.output.repository.AccountRepository;
import com.devsu.backendbank.infrastructure.output.repository.ClientRepository;
import com.devsu.backendbank.infrastructure.output.repository.PersonRepository;
import com.devsu.backendbank.infrastructure.output.repository.entity.Account;
import com.devsu.backendbank.infrastructure.output.repository.entity.AccountType;
import com.devsu.backendbank.infrastructure.output.repository.entity.Client;
import com.devsu.backendbank.infrastructure.output.repository.entity.Gender;
import com.devsu.backendbank.infrastructure.output.repository.entity.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AccountLockRepositoryMySqlIT {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldLoadAccountByIdForUpdateInsideTransaction() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Long accountId = tx.execute(status -> createAccount("9100000001", "910001").getId());

        Optional<Account> lockedAccount = tx.execute(status -> accountRepository.findByIdForUpdate(accountId));

        assertThat(lockedAccount).isPresent();
        assertThat(lockedAccount.orElseThrow().getNumeroCuenta()).isEqualTo("910001");
    }

    @Test
    void shouldLoadAccountByNumberForUpdateInsideTransaction() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.execute(status -> createAccount("9100000002", "910002"));

        var lockedAccount = tx.execute(status -> accountRepository.findByNumeroCuentaForUpdate("910002"));

        assertThat(lockedAccount).isPresent();
        assertThat(lockedAccount.orElseThrow().getNumeroCuenta()).isEqualTo("910002");
    }

    private Account createAccount(String identificacion, String numeroCuenta) {
        LocalDateTime now = LocalDateTime.now();

        Person person = new Person();
        person.setNombre("Lock Tester");
        person.setGenero(Gender.MASCULINO);
        person.setEdad(35);
        person.setIdentificacion(identificacion);
        person.setDireccion("Quito");
        person.setTelefono("0988888888");
        person.setCreatedAt(now);
        person.setUpdatedAt(now);
        person = personRepository.save(person);

        Client client = new Client();
        client.setPerson(person);
        client.setContrasena("Secret123");
        client.setEstado(true);
        client.setCreatedAt(now);
        client.setUpdatedAt(now);
        client = clientRepository.save(client);

        Account account = new Account();
        account.setClient(client);
        account.setNumeroCuenta(numeroCuenta);
        account.setTipoCuenta(AccountType.AHORROS);
        account.setSaldoInicial(new BigDecimal("1000.00"));
        account.setEstado(true);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);
        return accountRepository.save(account);
    }
}