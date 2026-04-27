package com.devsu.backendbank.infrastructure.input;

import com.devsu.backendbank.application.input.port.TransactionApi;
import com.devsu.backendbank.infrastructure.input.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.NUMERO_CUENTA;
import static com.devsu.backendbank.application.service.support.ServiceTestFixtures.latestTransaction;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionApi transactionApi;

    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TransactionController(transactionApi, transactionMapper))
                .build();
    }

    @Test
    void shouldExposeAccountNumberInsteadOfAccountId() throws Exception {
        when(transactionApi.findTransactions(Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(latestTransaction())));

        mockMvc.perform(get("/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value(NUMERO_CUENTA))
                .andExpect(jsonPath("$[0].cuentaId").doesNotExist());
    }
}

