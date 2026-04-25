package com.devsu.backendbank.infrastructure.exception;

import com.devsu.backendbank.infrastructure.exception.message.BusinessErrorMessage;
import com.devsu.backendbank.infrastructure.exception.message.TechnicalErrorMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnNotFoundForBusinessException() throws Exception {
        mockMvc.perform(get("/test/business-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("BBB0001"))
                .andExpect(jsonPath("$.message").value("The specified client does not exist."))
                .andExpect(jsonPath("$.path").value("/test/business-not-found"));
    }

    @Test
    void shouldReturnUnprocessableEntityForInsufficientFunds() throws Exception {
        mockMvc.perform(get("/test/business-insufficient"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("BBB0005"))
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }

    @Test
    void shouldReturnBadRequestWithFieldErrorsForInvalidBody() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BBB0016"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void shouldReturnInternalServerErrorForUnhandledException() throws Exception {
        mockMvc.perform(get("/test/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("TBB0001"));
    }

    @Test
    void shouldReturnMappedStatusForTechnicalException() throws Exception {
        mockMvc.perform(get("/test/technical-timeout"))
                .andExpect(status().isRequestTimeout())
                .andExpect(jsonPath("$.status").value(408))
                .andExpect(jsonPath("$.code").value("TBB0014"));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/business-not-found")
        String businessNotFound() {
            throw new BusinessException(BusinessErrorMessage.CLIENT_NOT_FOUND);
        }

        @GetMapping("/test/business-insufficient")
        String insufficientFunds() {
            throw new BusinessException(BusinessErrorMessage.INSUFFICIENT_FUNDS);
        }

        @GetMapping("/test/technical-timeout")
        String technicalTimeout() {
            throw new TechnicalException(TechnicalErrorMessage.TIMEOUT_ERROR);
        }

        @GetMapping("/test/unhandled")
        String unhandled() {
            throw new RuntimeException("boom");
        }

        @PostMapping("/test/validate")
        String validate(@Valid @RequestBody Input input) {
            return input.name();
        }
    }

    record Input(@NotBlank String name) {}
}


