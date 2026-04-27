package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.output.port.BankDb;
import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.infrastructure.exception.BusinessException;
import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.ACCOUNT_ID;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.ANOTHER_NUMERO_CUENTA;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.currentAccount;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.currentClient;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.incomingAccount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final Pageable UNPAGED = Pageable.unpaged();

    @Mock
    private BankDb bankDb;

    @InjectMocks
    private AccountService accountService;

    @Nested
    class FindAccounts {

        @Test
        void shouldReturnAccountsPage() {
            AccountDomain account = currentAccount();
            when(bankDb.findAccounts(UNPAGED)).thenReturn(new PageImpl<>(java.util.List.of(account)));

            var result = accountService.findAccounts(UNPAGED);

            assertThat(result.getContent()).containsExactly(account);
        }

        @Test
        void shouldReturnAccountById() {
            AccountDomain account = currentAccount();
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(account));

            AccountDomain result = accountService.findAccountById(ACCOUNT_ID);

            assertThat(result).isEqualTo(account);
        }

        @Test
        void shouldFailWhenAccountDoesNotExist() {
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> accountService.findAccountById(ACCOUNT_ID), BusinessErrorMessage.ACCOUNT_NOT_FOUND);
        }
    }

    @Nested
    class CreateAccount {

        @Test
        void shouldCreateAccountWhenClientExistsAndNumberIsAvailable() {
            AccountDomain incoming = incomingAccount();
            AccountDomain saved = currentAccount();
            when(bankDb.findClientById(incoming.clientId())).thenReturn(Optional.of(mock(ClientDomain.class)));
            when(bankDb.accountExistsByNumeroCuenta(incoming.numeroCuenta())).thenReturn(false);
            when(bankDb.saveOrUpdateAccount(incoming)).thenReturn(saved);

            AccountDomain result = accountService.createAccount(incoming);

            assertThat(result).isEqualTo(saved);
        }

        @Test
        void shouldFailCreatingAccountWhenClientDoesNotExist() {
            AccountDomain incoming = incomingAccount();
            when(bankDb.findClientById(incoming.clientId())).thenReturn(Optional.empty());

            assertBusinessException(() -> accountService.createAccount(incoming), BusinessErrorMessage.CLIENT_NOT_FOUND);
            verify(bankDb, never()).saveOrUpdateAccount(any());
        }

        @Test
        void shouldFailCreatingAccountWhenNumberAlreadyExists() {
            AccountDomain incoming = incomingAccount();
            when(bankDb.findClientById(incoming.clientId())).thenReturn(Optional.of(currentClient()));
            when(bankDb.accountExistsByNumeroCuenta(incoming.numeroCuenta())).thenReturn(true);

            assertBusinessException(() -> accountService.createAccount(incoming), BusinessErrorMessage.ACCOUNT_ALREADY_EXISTS);
            verify(bankDb, never()).saveOrUpdateAccount(any());
        }
    }

    @Nested
    class UpdateAccount {

        @Test
        void shouldPreserveIdAndAuditOnUpdate() {
            AccountDomain current = currentAccount();
            AccountDomain incoming = incomingAccount();
            AccountDomain saved = currentAccount();

            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(current));
            when(bankDb.findClientById(incoming.clientId())).thenReturn(Optional.of(currentClient()));
            when(bankDb.accountExistsByNumeroCuentaExcludingId(incoming.numeroCuenta(), ACCOUNT_ID)).thenReturn(false);
            when(bankDb.saveOrUpdateAccount(any(AccountDomain.class))).thenReturn(saved);

            AccountDomain result = accountService.updateAccount(ACCOUNT_ID, incoming);

            ArgumentCaptor<AccountDomain> captor = ArgumentCaptor.forClass(AccountDomain.class);
            verify(bankDb).saveOrUpdateAccount(captor.capture());
            AccountDomain persisted = captor.getValue();

            assertThat(result).isEqualTo(saved);
            assertThat(persisted.id()).isEqualTo(current.id());
            assertThat(persisted.createdAt()).isEqualTo(current.createdAt());
            assertThat(persisted.updatedAt()).isEqualTo(current.updatedAt());
            assertThat(persisted.nombreCliente()).isEqualTo(current.nombreCliente());
            assertThat(persisted.clientId()).isEqualTo(incoming.clientId());
            assertThat(persisted.numeroCuenta()).isEqualTo(incoming.numeroCuenta());
            assertThat(persisted.tipoCuenta()).isEqualTo(incoming.tipoCuenta());
            assertThat(persisted.estado()).isEqualTo(incoming.estado());
        }

        @Test
        void shouldFailUpdatingUnknownAccount() {
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> accountService.updateAccount(ACCOUNT_ID, incomingAccount()), BusinessErrorMessage.ACCOUNT_NOT_FOUND);
            verify(bankDb, never()).saveOrUpdateAccount(any());
        }

        @Test
        void shouldFailUpdatingWhenClientDoesNotExist() {
            AccountDomain current = currentAccount();
            AccountDomain incoming = incomingAccount();
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(current));
            when(bankDb.findClientById(incoming.clientId())).thenReturn(Optional.empty());

            assertBusinessException(() -> accountService.updateAccount(ACCOUNT_ID, incoming), BusinessErrorMessage.CLIENT_NOT_FOUND);
            verify(bankDb, never()).saveOrUpdateAccount(any());
        }

        @Test
        void shouldFailUpdatingWhenNumberBelongsToAnotherAccount() {
            AccountDomain current = currentAccount();
            AccountDomain incoming = incomingAccount();
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(current));
            when(bankDb.findClientById(incoming.clientId())).thenReturn(Optional.of(currentClient()));
            when(bankDb.accountExistsByNumeroCuentaExcludingId(incoming.numeroCuenta(), ACCOUNT_ID)).thenReturn(true);

            assertBusinessException(() -> accountService.updateAccount(ACCOUNT_ID, incoming), BusinessErrorMessage.ACCOUNT_ALREADY_EXISTS);
            verify(bankDb, never()).saveOrUpdateAccount(any());
        }
    }

    @Nested
    class PatchAccount {

        @ParameterizedTest
        @EnumSource(PatchMode.class)
        void shouldPreserveIdAndAuditOnPatch(PatchMode patchMode) {
            AccountDomain current = currentAccount();
            AccountDomain incoming = patchMode == PatchMode.CHANGED_NUMBER
                    ? incomingAccount()
                    : new AccountDomain(
                    999L,
                    current.clientId(),
                    current.numeroCuenta(),
                    current.tipoCuenta(),
                    current.saldoInicial(),
                    current.estado(),
                    null,
                    null,
                    current.nombreCliente()
            );
            AccountDomain saved = currentAccount();

            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(current));
            if (patchMode == PatchMode.CHANGED_NUMBER) {
                when(bankDb.accountExistsByNumeroCuentaExcludingId(incoming.numeroCuenta(), ACCOUNT_ID)).thenReturn(false);
            }
            when(bankDb.saveOrUpdateAccount(any(AccountDomain.class))).thenReturn(saved);

            AccountDomain result = accountService.patchAccount(ACCOUNT_ID, incoming);

            ArgumentCaptor<AccountDomain> captor = ArgumentCaptor.forClass(AccountDomain.class);
            verify(bankDb).saveOrUpdateAccount(captor.capture());
            AccountDomain persisted = captor.getValue();

            assertThat(result).isEqualTo(saved);
            assertThat(persisted.id()).isEqualTo(current.id());
            assertThat(persisted.createdAt()).isEqualTo(current.createdAt());
            assertThat(persisted.updatedAt()).isEqualTo(current.updatedAt());
            assertThat(persisted.nombreCliente()).isEqualTo(current.nombreCliente());

            if (patchMode == PatchMode.SAME_NUMBER) {
                verify(bankDb, never()).accountExistsByNumeroCuentaExcludingId(any(), any());
                assertThat(persisted.numeroCuenta()).isEqualTo(current.numeroCuenta());
            }
        }

        @Test
        void shouldFailPatchingUnknownAccount() {
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> accountService.patchAccount(ACCOUNT_ID, incomingAccount()), BusinessErrorMessage.ACCOUNT_NOT_FOUND);
            verify(bankDb, never()).saveOrUpdateAccount(any());
        }

        @Test
        void shouldFailPatchingWhenChangedNumberAlreadyExists() {
            AccountDomain current = currentAccount();
            AccountDomain incoming = incomingAccount();
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(current));
            when(bankDb.accountExistsByNumeroCuentaExcludingId(ANOTHER_NUMERO_CUENTA, ACCOUNT_ID)).thenReturn(true);

            assertBusinessException(() -> accountService.patchAccount(ACCOUNT_ID, incoming), BusinessErrorMessage.ACCOUNT_ALREADY_EXISTS);
            verify(bankDb, never()).saveOrUpdateAccount(any());
        }
    }

    @Nested
    class DeleteAccount {

        @Test
        void shouldDeleteAccountWhenItExists() {
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.of(currentAccount()));

            accountService.deleteAccount(ACCOUNT_ID);

            verify(bankDb).deleteAccountById(ACCOUNT_ID);
        }

        @Test
        void shouldFailDeletingUnknownAccount() {
            when(bankDb.findAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> accountService.deleteAccount(ACCOUNT_ID), BusinessErrorMessage.ACCOUNT_NOT_FOUND);
            verify(bankDb, never()).deleteAccountById(any());
        }
    }

    private void assertBusinessException(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
                                         BusinessErrorMessage expectedMessage) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting(throwable -> ((BusinessException) throwable).getBusinessErrorMessage())
                .isEqualTo(expectedMessage);
    }

    private enum PatchMode {
        CHANGED_NUMBER,
        SAME_NUMBER
    }
}

