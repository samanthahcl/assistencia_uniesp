package br.edu.uniesp.assistencia.internal.ordemservico.dto;

import br.edu.uniesp.assistencia.internal.ordemservico.entity.StatusOrdemServico;

import java.time.LocalDateTime;

public record CriarOrdemServicoResponse(
        Long id,
        String numeroProtocolo,
        StatusOrdemServico status,
        LocalDateTime dataAbertura
) {
}
