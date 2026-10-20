CREATE TABLE ordens_servico
(
    id                 BIGSERIAL    NOT NULL,
    numero_protocolo   VARCHAR(30)  NOT NULL,
    cliente_id         BIGINT       NOT NULL,
    equipamento_id     BIGINT       NOT NULL,
    tecnico_id         BIGINT,
    status             VARCHAR(30)  NOT NULL,
    descricao_problema TEXT         NOT NULL,
    data_abertura      TIMESTAMP    NOT NULL,
    data_finalizacao   TIMESTAMP,
    data_entrega       TIMESTAMP,

    CONSTRAINT pk_ordens_servico PRIMARY KEY (id),
    -- RN04: protocolo único (a aplicação reconhece esta constraint para tentar novo protocolo)
    CONSTRAINT uk_ordens_servico_numero_protocolo UNIQUE (numero_protocolo),
    CONSTRAINT ck_ordens_servico_protocolo_formato
        CHECK (numero_protocolo ~ '^OS-[0-9]{8}-[A-Z0-9]{4}$'),
    CONSTRAINT fk_ordens_servico_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_ordens_servico_equipamento
        FOREIGN KEY (equipamento_id) REFERENCES equipamentos (id),
    CONSTRAINT fk_ordens_servico_tecnico
        FOREIGN KEY (tecnico_id) REFERENCES tecnicos (id)
);

CREATE INDEX idx_ordens_servico_cliente ON ordens_servico (cliente_id);
CREATE INDEX idx_ordens_servico_status ON ordens_servico (status);

-- RN14: auditoria de status
CREATE TABLE historico_status
(
    id               BIGSERIAL    NOT NULL,
    ordem_servico_id BIGINT       NOT NULL,
    status_anterior  VARCHAR(30),
    novo_status      VARCHAR(30)  NOT NULL,
    data_alteracao   TIMESTAMP    NOT NULL,
    responsavel      VARCHAR(150) NOT NULL,
    observacao       VARCHAR(500),

    CONSTRAINT pk_historico_status PRIMARY KEY (id),
    CONSTRAINT fk_historico_status_ordem
        FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id)
);

CREATE INDEX idx_historico_status_ordem ON historico_status (ordem_servico_id);
