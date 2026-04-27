package com.devsu.backendbank;

import com.devsu.backendbank.infrastructure.output.repository.AccountRepository;
import com.devsu.backendbank.infrastructure.output.repository.ClientRepository;
import com.devsu.backendbank.infrastructure.output.repository.PersonRepository;
import com.devsu.backendbank.infrastructure.output.repository.entity.Account;
import com.devsu.backendbank.infrastructure.output.repository.entity.AccountType;
import com.devsu.backendbank.infrastructure.output.repository.entity.Client;
import com.devsu.backendbank.infrastructure.output.repository.entity.Gender;
import com.devsu.backendbank.infrastructure.output.repository.entity.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AccountRepositoryMySqlIT {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate tx;

	@BeforeEach
	void setUp() {
		tx = new TransactionTemplate(transactionManager);
	}

	@Test
	void shouldReturnTrueWhenNumberExistsInAnotherAccount() {
		Long firstId = tx.execute(status -> createAccount("9200000001", "920001").getId());
		tx.execute(status -> createAccount("9200000002", "920002").getId());

		Boolean existsInAnother = tx.execute(status -> accountRepository.existsByNumeroCuentaAndIdNot("920002", firstId));

		assertThat(existsInAnother).isTrue();
	}

	@Test
	void shouldLockByAccountNumber() {
		tx.execute(status -> createAccount("9200000003", "920003").getId());

		var locked = tx.execute(status -> accountRepository.findByNumeroCuentaForUpdate("920003"));

		assertThat(locked).isPresent();
		assertThat(locked.orElseThrow().getNumeroCuenta()).isEqualTo("920003");
	}

	private Account createAccount(String identificacion, String numeroCuenta) {
		LocalDateTime now = LocalDateTime.now();

		Person person = new Person();
		person.setNombre("Repo Tester " + identificacion);
		person.setGenero(Gender.MASCULINO);
		person.setEdad(40);
		person.setIdentificacion(identificacion);
		person.setDireccion("Quito");
		person.setTelefono("0977777777");
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
		account.setSaldoInicial(new BigDecimal("500.00"));
		account.setEstado(true);
		account.setCreatedAt(now);
		account.setUpdatedAt(now);
		return accountRepository.save(account);
	}
}


