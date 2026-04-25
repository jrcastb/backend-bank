package com.devsu.backendbank.infrastructure.input;

import com.devsu.backendbank.application.input.port.AccountApi;
import com.devsu.backendbank.domain.model.AccountDomain;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountCreateRequest;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountPatchRequest;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountResponse;
import com.devsu.backendbank.infrastructure.input.dto.account.AccountUpdateRequest;
import com.devsu.backendbank.infrastructure.input.mapper.AccountMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@Tag(name = "Cuentas")
public class AccountController {

    private final AccountApi accountApi;
    private final AccountMapper accountMapper;

    public AccountController(AccountApi accountApi, AccountMapper accountMapper) {
        this.accountApi = accountApi;
        this.accountMapper = accountMapper;
    }

    @GetMapping
    @Operation(summary = "Listar cuentas")
    public List<AccountResponse> findAll() {
        return accountApi.findAccounts(org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por id")
    public AccountResponse findById(@PathVariable Long id) {
        return accountMapper.toResponse(accountApi.findAccountById(id));
    }

    @PostMapping
    @Operation(summary = "Crear cuenta")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreateRequest request) {
        AccountDomain saved = accountApi.createAccount(accountMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplazar cuenta")
    public AccountResponse update(@PathVariable Long id, @Valid @RequestBody AccountUpdateRequest request) {
        return accountMapper.toResponse(accountApi.updateAccount(id, accountMapper.toDomain(id, request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente cuenta")
    public AccountResponse patch(@PathVariable Long id, @Valid @RequestBody AccountPatchRequest request) {
        AccountDomain current = accountApi.findAccountById(id);
        AccountDomain merged = accountMapper.merge(current, request);
        return accountMapper.toResponse(accountApi.patchAccount(id, merged));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cuenta")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountApi.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
