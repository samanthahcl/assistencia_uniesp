package br.edu.uniesp.assistencia.internal.ordemservico.entity;

public enum StatusOrdemServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    APROVADA,
    REJEITADA,
    EM_MANUTENCAO,
    AGUARDANDO_PECA,
    CONCLUIDA,
    ENTREGUE,
    CANCELADA
}
