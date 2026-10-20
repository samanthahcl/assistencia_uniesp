package br.edu.uniesp.assistencia.internal.cliente.controller;

import br.edu.uniesp.assistencia.internal.cliente.dto.AtualizarClienteRequest;
import br.edu.uniesp.assistencia.internal.cliente.dto.ClienteResumoResponse;
import br.edu.uniesp.assistencia.internal.cliente.dto.CriarClienteRequest;
import br.edu.uniesp.assistencia.internal.cliente.dto.CriarClienteResponse;
import br.edu.uniesp.assistencia.internal.cliente.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;


    @PostMapping
    public ResponseEntity<CriarClienteResponse> criar(@Valid @RequestBody CriarClienteRequest request) {
        CriarClienteResponse response = clienteService.criarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResumoResponse> buscarPorId(@PathVariable Long id) {
        ClienteResumoResponse response = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ClienteResumoResponse>> listarTodos(
            @org.springframework.data.web.PageableDefault(size = 10) Pageable pageable) {
        org.springframework.data.domain.Page<ClienteResumoResponse> page = clienteService.listarTodos(pageable);
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClienteResumoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarClienteRequest request) {
        ClienteResumoResponse response = clienteService.atualizarCliente(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        clienteService.inativarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        clienteService.ativarCliente(id);

        return ResponseEntity.noContent().build();
    }

}
