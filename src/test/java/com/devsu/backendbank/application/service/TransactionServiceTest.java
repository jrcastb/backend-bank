package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.output.port.AccountLockPort;
import com.devsu.backendbank.application.output.port.TransactionCommandPort;
import com.devsu.backendbank.application.output.port.TransactionQueryPort;
import com.devsu.backendbank.application.service.transaction.CreditTransactionRuleStrategy;
import com.devsu.backendbank.application.service.transaction.DebitTransactionRuleStrategy;
import com.devsu.backendbank.application.service.transaction.TransactionRuleStrategyResolver;
import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.domain.enums.TransactionTypeDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.stream.Stream;

import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.ACCOUNT_ID;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.CREDITO_500;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.DEBITO_300;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.LIMITE_DIARIO;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.SALDO_INICIAL;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.TRANSACTION_ID;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.creditTransactionInput;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.currentAccount;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.debitTransactionInput;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.inactiveAccount;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.latestTransaction;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Pageable UNPAGED = Pageable.unpaged();

    @Mock
    private AccountLockPort accountLockPort;

    @Mock
    private TransactionQueryPort transactionQueryPort;

    @Mock
    private TransactionCommandPort transactionCommandPort;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        TransactionRuleStrategyResolver resolver = new TransactionRuleStrategyResolver(java.util.List.of(
                new CreditTransactionRuleStrategy(),
                new DebitTransactionRuleStrategy(transactionQueryPort)
        ));
        transactionService = new TransactionService(accountLockPort, transactionQueryPort, transactionCommandPort, resolver);
    }

    @Nested
    class FindTransactions {

        @Test
        void shouldReturnTransactionsPage() {
            TransactionDomain transaction = latestTransaction();
            when(transactionQueryPort.findTransactions(UNPAGED)).thenReturn(new PageImpl<>(java.util.List.of(transaction)));

            var result = transactionService.findTransactions(UNPAGED);

            assertThat(result.getContent()).containsExactly(transaction);
        }

        @Test
        void shouldReturnTransactionById() {
            TransactionDomain transaction = latestTransaction();
            when(transactionQueryPort.findTransactionById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

            TransactionDomain result = transactionService.findTransactionById(TRANSACTION_ID);

            assertThat(result).isEqualTo(transaction);
        }

        @Test
        void shouldFailWhenTransactionDoesNotExist() {
            when(transactionQueryPort.findTransactionById(TRANSACTION_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> transactionService.findTransactionById(TRANSACTION_ID), BusinessErrorMessage.NO_MOVEMENTS_FOUND);
        }
    }

    @Nested
    class CreateTransaction {

        @Test
        void shouldFailWhenAccountDoesNotExist() {
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> transactionService.createTransaction(creditTransactionInput()), BusinessErrorMessage.ACCOUNT_NOT_FOUND);
            verify(transactionCommandPort, never()).saveTransaction(any());
        }

        @Test
        void shouldFailWhenAccountIsInactive() {
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(inactiveAccount()));

            assertBusinessException(() -> transactionService.createTransaction(creditTransactionInput()), BusinessErrorMessage.ACCOUNT_INACTIVE);
            verify(transactionCommandPort, never()).saveTransaction(any());
        }

        @ParameterizedTest
        @MethodSource("invalidAmounts")
        void shouldFailWhenAmountIsNullOrZero(BigDecimal invalidAmount) {
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(currentAccount()));
            TransactionDomain input = new TransactionDomain(
                    null,
                    ACCOUNT_ID,
                    null,
                    TransactionTypeDomain.CREDITO,
                    invalidAmount,
                    null,
                    null,
                    currentAccount().numeroCuenta()
            );

            assertBusinessException(() -> transactionService.createTransaction(input), BusinessErrorMessage.INVALID_TRANSACTION_AMOUNT);
            verify(transactionCommandPort, never()).saveTransaction(any());
        }

        @Test
        void shouldCreateCreditUsingInitialBalanceWhenThereIsNoPreviousTransaction() {
            AccountDomain account = currentAccount();
            TransactionDomain input = creditTransactionInput();
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(transactionQueryPort.findLatestTransactionByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());
            when(transactionCommandPort.saveTransaction(any(TransactionDomain.class))).thenAnswer(invocation -> {
                TransactionDomain persisted = invocation.getArgument(0);
                return new TransactionDomain(
                        TRANSACTION_ID,
                        persisted.accountId(),
                        persisted.fecha(),
                        persisted.tipoMovimiento(),
                        persisted.valor(),
                        persisted.saldo(),
                        persisted.createdAt(),
                        persisted.numeroCuenta()
                );
            });

            LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC);
            TransactionDomain result = transactionService.createTransaction(input);
            LocalDateTime after = LocalDateTime.now(ZoneOffset.UTC);

            ArgumentCaptor<TransactionDomain> captor = ArgumentCaptor.forClass(TransactionDomain.class);
            verify(transactionCommandPort).saveTransaction(captor.capture());
            TransactionDomain persisted = captor.getValue();

            assertThat(result.id()).isEqualTo(TRANSACTION_ID);
            assertThat(persisted.accountId()).isEqualTo(ACCOUNT_ID);
            assertThat(persisted.tipoMovimiento()).isEqualTo(TransactionTypeDomain.CREDITO);
            assertThat(persisted.valor()).isEqualByComparingTo(CREDITO_500);
            assertThat(persisted.saldo()).isEqualByComparingTo(SALDO_INICIAL.add(CREDITO_500));
            assertThat(persisted.createdAt()).isNull();
            assertThat(persisted.numeroCuenta()).isEqualTo(account.numeroCuenta());
            assertThat(persisted.fecha()).isBetween(before, after);
            verify(transactionQueryPort, never()).sumDailyDebitsByAccount(anyLong(), any(), any());
        }

        @Test
        void shouldCreateDebitUsingLatestBalanceWhenRulesPass() {
            AccountDomain account = currentAccount();
            TransactionDomain input = debitTransactionInput();
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(account));
            when(transactionQueryPort.findLatestTransactionByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(latestTransaction()));
            when(transactionQueryPort.sumDailyDebitsByAccount(anyLong(), any(), any())).thenReturn(new BigDecimal("600.00"));
            when(transactionCommandPort.saveTransaction(any(TransactionDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TransactionDomain result = transactionService.createTransaction(input);

            assertThat(result.saldo()).isEqualByComparingTo("1400.00");
            assertThat(result.valor()).isEqualByComparingTo(DEBITO_300);
            verify(transactionQueryPort).sumDailyDebitsByAccount(anyLong(), any(), any());
        }

        @Test
        void shouldAllowDebitExactlyAtDailyLimit() {
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(currentAccount()));
            when(transactionQueryPort.findLatestTransactionByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(latestTransaction()));
            when(transactionQueryPort.sumDailyDebitsByAccount(anyLong(), any(), any())).thenReturn(LIMITE_DIARIO.subtract(DEBITO_300.abs()));
            when(transactionCommandPort.saveTransaction(any(TransactionDomain.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TransactionDomain result = transactionService.createTransaction(debitTransactionInput());

            assertThat(result.saldo()).isEqualByComparingTo("1400.00");
        }

        @Test
        void shouldFailWhenFundsAreInsufficient() {
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(currentAccount()));
            when(transactionQueryPort.findLatestTransactionByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(new TransactionDomain(
                    TRANSACTION_ID,
                    ACCOUNT_ID,
                    LocalDateTime.now(ZoneOffset.UTC),
                    TransactionTypeDomain.CREDITO,
                    CREDITO_500,
                    new BigDecimal("200.00"),
                    LocalDateTime.now(ZoneOffset.UTC),
                    currentAccount().numeroCuenta()
            )));

            assertBusinessException(() -> transactionService.createTransaction(debitTransactionInput()), BusinessErrorMessage.INSUFFICIENT_FUNDS);
            verify(transactionQueryPort, never()).sumDailyDebitsByAccount(anyLong(), any(), any());
            verify(transactionCommandPort, never()).saveTransaction(any());
        }

        @Test
        void shouldFailWhenDailyLimitWouldBeExceeded() {
            when(accountLockPort.findAccountByIdForUpdate(ACCOUNT_ID)).thenReturn(Optional.of(currentAccount()));
            when(transactionQueryPort.findLatestTransactionByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(latestTransaction()));
            when(transactionQueryPort.sumDailyDebitsByAccount(anyLong(), any(), any())).thenReturn(new BigDecimal("800.01"));

            assertBusinessException(() -> transactionService.createTransaction(debitTransactionInput()), BusinessErrorMessage.DAILY_LIMIT_EXCEEDED);
            verify(transactionCommandPort, never()).saveTransaction(any());
        }

        private static Stream<Arguments> invalidAmounts() {
            return Stream.of(
                    Arguments.of((BigDecimal) null),
                    Arguments.of(BigDecimal.ZERO)
            );
        }
    }

    @Nested
    class DeleteTransaction {

        @Test
        void shouldDeleteTransactionWhenItExists() {
            when(transactionQueryPort.findTransactionById(TRANSACTION_ID)).thenReturn(Optional.of(latestTransaction()));

            transactionService.deleteTransaction(TRANSACTION_ID);

            verify(transactionCommandPort).deleteTransactionById(TRANSACTION_ID);
        }

        @Test
        void shouldFailDeletingUnknownTransaction() {
            when(transactionQueryPort.findTransactionById(TRANSACTION_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> transactionService.deleteTransaction(TRANSACTION_ID), BusinessErrorMessage.NO_MOVEMENTS_FOUND);
            verify(transactionCommandPort, never()).deleteTransactionById(anyLong());
        }
    }

    private void assertBusinessException(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
                                         BusinessErrorMessage expectedMessage) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting(throwable -> ((BusinessException) throwable).getBusinessErrorMessage())
                .isEqualTo(expectedMessage);
    }

}



