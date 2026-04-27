package com.devsu.backendbank.infrastructure.input;

import com.devsu.backendbank.application.input.port.ClientApi;
import com.devsu.backendbank.domain.model.ClientDomain;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientCreateRequest;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientPatchRequest;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientResponse;
import com.devsu.backendbank.infrastructure.input.dto.client.ClientUpdateRequest;
import com.devsu.backendbank.infrastructure.input.mapper.ClientMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/clientes")
@Tag(name = "Clientes")
@RequiredArgsConstructor
public class ClientController {

    private final ClientApi clientApi;
    private final ClientMapper clientMapper;

    @GetMapping
    @Operation(summary = "Listar clientes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de clientes"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public List<ClientResponse> findAll() {
        return clientApi.findClients(org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por id")
    public ClientResponse findById(@PathVariable Long id) {
        return clientMapper.toResponse(clientApi.findClientById(id));
    }

    @PostMapping
    @Operation(summary = "Crear cliente")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientCreateRequest request) {
        ClientDomain saved = clientApi.createClient(clientMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(clientMapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplazar cliente")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientUpdateRequest request) {
        return clientMapper.toResponse(clientApi.updateClient(id, clientMapper.toDomain(id, request)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente cliente")
    public ClientResponse patch(@PathVariable Long id, @Valid @RequestBody ClientPatchRequest request) {
        ClientDomain current = clientApi.findClientById(id);
        ClientDomain merged = clientMapper.merge(current, request);
        return clientMapper.toResponse(clientApi.patchClient(id, merged));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientApi.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
