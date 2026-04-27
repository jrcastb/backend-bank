package com.devsu.backendbank;

import com.devsu.backendbank.infrastructure.output.repository.AccountRepository;
import com.devsu.backendbank.infrastructure.output.repository.ClientRepository;
import com.devsu.backendbank.infrastructure.output.repository.PersonRepository;
import com.devsu.backendbank.infrastructure.output.repository.TransactionRepository;
import com.devsu.backendbank.infrastructure.output.repository.entity.Account;
import com.devsu.backendbank.infrastructure.output.repository.entity.AccountType;
import com.devsu.backendbank.infrastructure.output.repository.entity.Client;
import com.devsu.backendbank.infrastructure.output.repository.entity.Gender;
import com.devsu.backendbank.infrastructure.output.repository.entity.Person;
import com.devsu.backendbank.infrastructure.output.repository.entity.Transaction;
import com.devsu.backendbank.infrastructure.output.repository.entity.TransactionType;
import com.devsu.backendbank.infrastructure.output.repository.projection.TransactionReportProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TransactionRepositoryMySqlIT {

	private static final LocalDateTime FROM = LocalDateTime.of(2026, 4, 1, 0, 0, 0);
	private static final LocalDateTime TO = LocalDateTime.of(2026, 4, 1, 23, 59, 59);

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate tx;

	@BeforeEach
	void setUp() {
		tx = new TransactionTemplate(transactionManager);
	}

	@Test
	void shouldReturnReportDataOrderedAndIncludingDateBoundaries() {
		Long clientId = tx.execute(status -> {
			ClientData data = createClientWithAccount("9000000001", "900001");

			createMovement(data.account(), LocalDateTime.of(2026, 3, 31, 23, 59, 59), TransactionType.CREDITO,
					new BigDecimal("100.00"), new BigDecimal("2100.00"));
			createMovement(data.account(), LocalDateTime.of(2026, 4, 1, 0, 0, 0), TransactionType.DEBITO,
					new BigDecimal("-200.00"), new BigDecimal("1900.00"));
			createMovement(data.account(), LocalDateTime.of(2026, 4, 1, 12, 30, 0), TransactionType.CREDITO,
					new BigDecimal("50.00"), new BigDecimal("1950.00"));
			createMovement(data.account(), LocalDateTime.of(2026, 4, 1, 23, 59, 59), TransactionType.DEBITO,
					new BigDecimal("-100.00"), new BigDecimal("1850.00"));

			ClientData anotherClient = createClientWithAccount("9000000002", "900002");
			createMovement(anotherClient.account(), LocalDateTime.of(2026, 4, 1, 10, 0, 0), TransactionType.CREDITO,
					new BigDecimal("999.00"), new BigDecimal("1099.00"));

			return data.client().getId();
		});

		List<TransactionReportProjection> reportRows = tx.execute(status -> transactionRepository
				.findReportByClientAndDateRange(clientId, FROM, TO, Pageable.unpaged())
				.getContent());

		assertThat(reportRows).hasSize(3);
		assertThat(reportRows)
				.extracting(TransactionReportProjection::getFecha)
				.containsExactly(
						LocalDateTime.of(2026, 4, 1, 0, 0, 0),
						LocalDateTime.of(2026, 4, 1, 12, 30, 0),
						LocalDateTime.of(2026, 4, 1, 23, 59, 59)
				);
		assertThat(reportRows)
				.extracting(TransactionReportProjection::getMovimiento)
				.containsExactly(
						new BigDecimal("-200.00"),
						new BigDecimal("50.00"),
						new BigDecimal("-100.00")
				);
	}

	@Test
	void shouldCalculateDebitAggregatesByAccountAndClientAndReturnZeroWhenNoRows() {
		DebitsContext context = tx.execute(status -> {
			ClientData data = createClientWithAccount("9000000003", "900003");
			Account secondAccount = createAccount(data.client(), "900004");

			createMovement(data.account(), LocalDateTime.of(2026, 4, 1, 9, 0, 0), TransactionType.DEBITO,
					new BigDecimal("-100.00"), new BigDecimal("1900.00"));
			createMovement(data.account(), LocalDateTime.of(2026, 4, 1, 11, 0, 0), TransactionType.DEBITO,
					new BigDecimal("-250.00"), new BigDecimal("1650.00"));
			createMovement(data.account(), LocalDateTime.of(2026, 4, 1, 15, 0, 0), TransactionType.CREDITO,
					new BigDecimal("300.00"), new BigDecimal("1950.00"));
			createMovement(data.account(), LocalDateTime.of(2026, 4, 2, 8, 0, 0), TransactionType.DEBITO,
					new BigDecimal("-75.00"), new BigDecimal("1875.00"));
			createMovement(secondAccount, LocalDateTime.of(2026, 4, 1, 13, 0, 0), TransactionType.DEBITO,
					new BigDecimal("-50.00"), new BigDecimal("950.00"));

			return new DebitsContext(data.account().getId(), data.client().getId());
		});

		BigDecimal accountDebits = tx.execute(status -> transactionRepository
				.sumDailyDebitsByAccount(context.accountId(), FROM, TO, TransactionType.DEBITO));
		BigDecimal clientDebits = tx.execute(status -> transactionRepository
				.sumDailyDebitsByClient(context.clientId(), FROM, TO, TransactionType.DEBITO));
		BigDecimal emptyRangeDebits = tx.execute(status -> transactionRepository
				.sumDailyDebitsByAccount(context.accountId(),
						LocalDateTime.of(2026, 5, 1, 0, 0),
						LocalDateTime.of(2026, 5, 1, 23, 59, 59),
						TransactionType.DEBITO));

		assertThat(accountDebits).isEqualByComparingTo("350.00");
		assertThat(clientDebits).isEqualByComparingTo("400.00");
		assertThat(emptyRangeDebits).isEqualByComparingTo("0.00");
	}

	private ClientData createClientWithAccount(String identificacion, String numeroCuenta) {
		Person person = createPerson(identificacion);
		Client client = createClient(person);
		Account account = createAccount(client, numeroCuenta);
		return new ClientData(client, account);
	}

	private Person createPerson(String identificacion) {
					LocalDateTime now = LocalDateTime.now();

		Person person = new Person();
		person.setNombre("Cliente " + identificacion);
		person.setGenero(Gender.MASCULINO);
		person.setEdad(30);
		person.setIdentificacion(identificacion);
		person.setDireccion("Quito");
		person.setTelefono("0999999999");
					person.setCreatedAt(now);
					person.setUpdatedAt(now);
		return personRepository.save(person);
	}

	private Client createClient(Person person) {
					LocalDateTime now = LocalDateTime.now();

		Client client = new Client();
		client.setPerson(person);
		client.setContrasena("Secret123");
		client.setEstado(true);
					client.setCreatedAt(now);
					client.setUpdatedAt(now);
		return clientRepository.save(client);
	}

	private Account createAccount(Client client, String numeroCuenta) {
					LocalDateTime now = LocalDateTime.now();

		Account account = new Account();
		account.setClient(client);
		account.setNumeroCuenta(numeroCuenta);
		account.setTipoCuenta(AccountType.AHORROS);
		account.setSaldoInicial(new BigDecimal("2000.00"));
		account.setEstado(true);
					account.setCreatedAt(now);
					account.setUpdatedAt(now);
		return accountRepository.save(account);
	}

	private void createMovement(Account account,
								LocalDateTime fecha,
								TransactionType type,
								BigDecimal valor,
								BigDecimal saldo) {
		Transaction movement = new Transaction();
		movement.setAccount(account);
		movement.setFecha(fecha);
		movement.setTipoMovimiento(type);
		movement.setValor(valor);
		movement.setSaldo(saldo);
		transactionRepository.save(movement);
	}

	private record ClientData(Client client, Account account) {
	}

	private record DebitsContext(Long accountId, Long clientId) {
	}
}

