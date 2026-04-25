package com.devsu.backendbank.infrastructure.input;

import com.devsu.backendbank.application.input.port.TransactionApi;
import com.devsu.backendbank.domain.model.TransactionDomain;
import com.devsu.backendbank.infrastructure.input.dto.transaction.TransactionCreateRequest;
import com.devsu.backendbank.infrastructure.input.dto.transaction.TransactionResponse;
import com.devsu.backendbank.infrastructure.input.mapper.TransactionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/movimientos")
@Tag(name = "Movimientos")
public class TransactionController {

    private final TransactionApi transactionApi;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionApi transactionApi,
                                 TransactionMapper transactionMapper) {
        this.transactionApi = transactionApi;
        this.transactionMapper = transactionMapper;
    }

    @GetMapping
    @Operation(summary = "Listar movimientos")
    public List<TransactionResponse> findAll() {
        return transactionApi.findTransactions(org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento por id")
    public TransactionResponse findById(@PathVariable Long id) {
        return transactionMapper.toResponse(transactionApi.findTransactionById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar movimiento")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionCreateRequest request) {
        TransactionDomain saved = transactionApi.createTransaction(transactionMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionApi.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
