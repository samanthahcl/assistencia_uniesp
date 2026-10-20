package br.edu.uniesp.assistencia.internal.ordemservico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarOrdemServicoRequest(

        @NotNull(message = "O ID do cliente é obrigatório")
        Long clienteId,

        @NotNull(message = "O ID do equipamento é obrigatório")
        Long equipamentoId,

        @NotBlank(message = "A descrição do problema é obrigatória")
        @Size(max = 2000, message = "A descrição do problema deve ter no máximo 2000 caracteres")
        String descricaoProblema
) {
}
