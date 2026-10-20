package br.edu.uniesp.assistencia.internal.ordemservico.controller;

import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoRequest;
import br.edu.uniesp.assistencia.internal.ordemservico.dto.CriarOrdemServicoResponse;
import br.edu.uniesp.assistencia.internal.ordemservico.service.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/ordens")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    public ResponseEntity<CriarOrdemServicoResponse> abrir(@Valid @RequestBody CriarOrdemServicoRequest request) {
        CriarOrdemServicoResponse response = ordemServicoService.abrirOrdemServico(request);
        return ResponseEntity.created(URI.create("/ordens/" + response.id())).body(response);
    }
}
