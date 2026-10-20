package br.edu.uniesp.assistencia.internal.ordemservico.dto;

import jakarta.validation.constraints.NotBlank;

public record AtualizarOrdemServicoRequest(

        @NotBlank(message = "A descrição do problema é obrigatória")
        String descricaoProblema
) {
}
