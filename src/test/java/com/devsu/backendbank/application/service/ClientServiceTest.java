package com.devsu.backendbank.application.service;

import com.devsu.backendbank.application.output.port.BankDb;
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

import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.CLIENT_ID;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.PERSON_ID;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.currentClient;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.incomingClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    private static final Pageable UNPAGED = Pageable.unpaged();

    @Mock
    private BankDb bankDb;

    @InjectMocks
    private ClientService clientService;

    @Nested
    class FindClients {

        @Test
        void shouldReturnClientsPage() {
            ClientDomain client = currentClient();
            when(bankDb.findClients(UNPAGED)).thenReturn(new PageImpl<>(java.util.List.of(client)));

            var result = clientService.findClients(UNPAGED);

            assertThat(result.getContent()).containsExactly(client);
        }

        @Test
        void shouldReturnClientById() {
            ClientDomain client = currentClient();
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(client));

            ClientDomain result = clientService.findClientById(CLIENT_ID);

            assertThat(result).isEqualTo(client);
        }

        @Test
        void shouldFailWhenClientDoesNotExist() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> clientService.findClientById(CLIENT_ID), BusinessErrorMessage.CLIENT_NOT_FOUND);
        }
    }

    @Nested
    class CreateClient {

        @Test
        void shouldCreateClientWhenIdentificationIsAvailable() {
            ClientDomain incoming = incomingClient();
            ClientDomain saved = currentClient();
            when(bankDb.personExistsByIdentificacion(incoming.person().identificacion())).thenReturn(false);
            when(bankDb.saveOrUpdateClient(incoming)).thenReturn(saved);

            ClientDomain result = clientService.createClient(incoming);

            assertThat(result).isEqualTo(saved);
        }

        @Test
        void shouldFailWhenIdentificationAlreadyExists() {
            ClientDomain incoming = incomingClient();
            when(bankDb.personExistsByIdentificacion(incoming.person().identificacion())).thenReturn(true);

            assertBusinessException(() -> clientService.createClient(incoming), BusinessErrorMessage.CLIENT_ALREADY_EXISTS);
            verify(bankDb, never()).saveOrUpdateClient(any());
        }
    }

    @Nested
    class MutateClient {

        @ParameterizedTest
        @EnumSource(MutationType.class)
        void shouldPreserveIdentityAndAuditInformation(MutationType mutationType) {
            ClientDomain current = currentClient();
            ClientDomain incoming = incomingClient();
            ClientDomain saved = currentClient();

            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(current));
            when(bankDb.personExistsByIdentificacionExcludingId(incoming.person().identificacion(), PERSON_ID)).thenReturn(false);
            when(bankDb.saveOrUpdateClient(any(ClientDomain.class))).thenReturn(saved);

            ClientDomain result = executeMutation(mutationType, CLIENT_ID, incoming);

            ArgumentCaptor<ClientDomain> captor = ArgumentCaptor.forClass(ClientDomain.class);
            verify(bankDb).saveOrUpdateClient(captor.capture());
            ClientDomain persisted = captor.getValue();

            assertThat(result).isEqualTo(saved);
            assertThat(persisted.id()).isEqualTo(current.id());
            assertThat(persisted.createdAt()).isEqualTo(current.createdAt());
            assertThat(persisted.updatedAt()).isEqualTo(current.updatedAt());
            assertThat(persisted.contrasena()).isEqualTo(incoming.contrasena());
            assertThat(persisted.estado()).isEqualTo(incoming.estado());

            assertThat(persisted.person().id()).isEqualTo(current.person().id());
            assertThat(persisted.person().createdAt()).isEqualTo(current.person().createdAt());
            assertThat(persisted.person().updatedAt()).isEqualTo(current.person().updatedAt());
            assertThat(persisted.person().nombre()).isEqualTo(incoming.person().nombre());
            assertThat(persisted.person().identificacion()).isEqualTo(incoming.person().identificacion());
        }

        @ParameterizedTest
        @EnumSource(MutationType.class)
        void shouldFailWhenClientDoesNotExist(MutationType mutationType) {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> executeMutation(mutationType, CLIENT_ID, incomingClient()), BusinessErrorMessage.CLIENT_NOT_FOUND);
            verify(bankDb, never()).saveOrUpdateClient(any());
        }

        @ParameterizedTest
        @EnumSource(MutationType.class)
        void shouldFailWhenIdentificationBelongsToAnotherPerson(MutationType mutationType) {
            ClientDomain current = currentClient();
            ClientDomain incoming = incomingClient();
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(current));
            when(bankDb.personExistsByIdentificacionExcludingId(incoming.person().identificacion(), PERSON_ID)).thenReturn(true);

            assertBusinessException(() -> executeMutation(mutationType, CLIENT_ID, incoming), BusinessErrorMessage.CLIENT_ALREADY_EXISTS);
            verify(bankDb, never()).saveOrUpdateClient(any());
        }
    }

    @Nested
    class DeleteClient {

        @Test
        void shouldDeleteClientWhenItExists() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.of(currentClient()));

            clientService.deleteClient(CLIENT_ID);

            verify(bankDb).deleteClientById(CLIENT_ID);
        }

        @Test
        void shouldFailDeletingUnknownClient() {
            when(bankDb.findClientById(CLIENT_ID)).thenReturn(Optional.empty());

            assertBusinessException(() -> clientService.deleteClient(CLIENT_ID), BusinessErrorMessage.CLIENT_NOT_FOUND);
            verify(bankDb, never()).deleteClientById(any());
        }
    }

    private ClientDomain executeMutation(MutationType mutationType, Long id, ClientDomain incoming) {
        return switch (mutationType) {
            case UPDATE -> clientService.updateClient(id, incoming);
            case PATCH -> clientService.patchClient(id, incoming);
        };
    }

    private void assertBusinessException(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable,
                                         BusinessErrorMessage expectedMessage) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting(throwable -> ((BusinessException) throwable).getBusinessErrorMessage())
                .isEqualTo(expectedMessage);
    }

    private enum MutationType {
        UPDATE,
        PATCH
    }
}

