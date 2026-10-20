package br.edu.uniesp.assistencia.internal.ordemservico.dto;

import br.edu.uniesp.assistencia.internal.ordemservico.entity.StatusOrdemServico;

import java.time.LocalDateTime;

public record OrdemServicoResumoResponse(
        Long id,
        String numeroProtocolo,
        Long clienteId,
        String clienteNome,
        Long equipamentoId,
        String equipamentoModelo,
        Long tecnicoId,
        String tecnicoNome,
        StatusOrdemServico status,
        String descricaoProblema,
        LocalDateTime dataAbertura,
        LocalDateTime dataFinalizacao,
        LocalDateTime dataEntrega
) {
}
