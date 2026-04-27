package com.devsu.backendbank.infrastructure.output.repository;

import com.devsu.backendbank.TestcontainersConfiguration;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AccountLockRepositoryMySqlIT {

	private static final String IDENTIFICACION = "9300000001";
	private static final String NUMERO_CUENTA = "930001";

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Test
	@Transactional
	void shouldFindByIdForUpdateOnSameTransaction() {
		Account saved = createAccount();

		var locked = accountRepository.findByIdForUpdate(saved.getId());

		assertThat(locked).isPresent();
		assertThat(locked.orElseThrow().getId()).isEqualTo(saved.getId());
	}

	private Account createAccount() {
		LocalDateTime now = LocalDateTime.now();

		Person person = new Person();
		person.setNombre("Infra Repo Tester");
		person.setGenero(Gender.MASCULINO);
		person.setEdad(28);
		person.setIdentificacion(IDENTIFICACION);
		person.setDireccion("Quito");
		person.setTelefono("0966666666");
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
		account.setNumeroCuenta(NUMERO_CUENTA);
		account.setTipoCuenta(AccountType.AHORROS);
		account.setSaldoInicial(new BigDecimal("700.00"));
		account.setEstado(true);
		account.setCreatedAt(now);
		account.setUpdatedAt(now);
		return accountRepository.save(account);
	}
}



