package com.devsu.backendbank.infrastructure.input;

import com.devsu.backendbank.application.input.port.AccountApi;
import com.devsu.backendbank.infrastructure.input.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.ACCOUNT_ID;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.CLIENTE_NOMBRE;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.NUMERO_CUENTA;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.currentAccount;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountApi accountApi;

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AccountController(accountApi, accountMapper))
                .build();
    }

    @Test
    void shouldExposeClientNameInsteadOfClientId() throws Exception {
        when(accountApi.findAccountById(ACCOUNT_ID)).thenReturn(currentAccount());

        mockMvc.perform(get("/cuentas/{id}", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACCOUNT_ID))
                .andExpect(jsonPath("$.nombreCliente").value(CLIENTE_NOMBRE))
                .andExpect(jsonPath("$.numeroCuenta").value(NUMERO_CUENTA))
                .andExpect(jsonPath("$.clienteId").doesNotExist());
    }
}

