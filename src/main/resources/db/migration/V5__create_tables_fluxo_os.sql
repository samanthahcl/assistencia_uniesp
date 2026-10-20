CREATE TABLE diagnosticos
(
    id                BIGSERIAL NOT NULL,
    ordem_servico_id  BIGINT    NOT NULL,
    tecnico_id        BIGINT    NOT NULL,
    descricao_tecnica TEXT      NOT NULL,
    solucao_proposta  TEXT      NOT NULL,
    data_registro     TIMESTAMP NOT NULL,

    CONSTRAINT pk_diagnosticos PRIMARY KEY (id),
    CONSTRAINT uk_diagnosticos_ordem UNIQUE (ordem_servico_id),
    CONSTRAINT fk_diagnosticos_ordem FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id),
    CONSTRAINT fk_diagnosticos_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnicos (id)
);

CREATE TABLE orcamentos
(
    id                    BIGSERIAL      NOT NULL,
    ordem_servico_id      BIGINT         NOT NULL,
    valor_mao_de_obra     NUMERIC(10, 2) NOT NULL,
    valor_pecas           NUMERIC(10, 2) NOT NULL,
    valor_total           NUMERIC(10, 2) NOT NULL,
    data_criacao          TIMESTAMP      NOT NULL,
    data_expiracao        TIMESTAMP      NOT NULL,
    status                VARCHAR(20)    NOT NULL,
    data_resposta         TIMESTAMP,
    justificativa_cliente TEXT,

    CONSTRAINT pk_orcamentos PRIMARY KEY (id),
    CONSTRAINT uk_orcamentos_ordem UNIQUE (ordem_servico_id),
    CONSTRAINT fk_orcamentos_ordem FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id)
);

CREATE TABLE itens_orcamento
(
    id               BIGSERIAL      NOT NULL,
    orcamento_id     BIGINT         NOT NULL,
    peca_id          BIGINT         NOT NULL,
    quantidade       INTEGER        NOT NULL,
    preco_unitario   NUMERIC(10, 2) NOT NULL,
    valor_total_item NUMERIC(10, 2) NOT NULL,

    CONSTRAINT pk_itens_orcamento PRIMARY KEY (id),
    CONSTRAINT fk_itens_orcamento_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamentos (id),
    CONSTRAINT fk_itens_orcamento_peca FOREIGN KEY (peca_id) REFERENCES pecas (id)
);

CREATE TABLE os_pecas
(
    id                       BIGSERIAL      NOT NULL,
    ordem_servico_id         BIGINT         NOT NULL,
    peca_id                  BIGINT         NOT NULL,
    quantidade               INTEGER        NOT NULL,
    preco_unitario_historico NUMERIC(10, 2) NOT NULL,
    data_registro            TIMESTAMP      NOT NULL,

    CONSTRAINT pk_os_pecas PRIMARY KEY (id),
    CONSTRAINT fk_os_pecas_ordem FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id),
    CONSTRAINT fk_os_pecas_peca FOREIGN KEY (peca_id) REFERENCES pecas (id)
);

CREATE TABLE anexos
(
    id                   BIGSERIAL    NOT NULL,
    ordem_servico_id     BIGINT       NOT NULL,
    nome_original        VARCHAR(255) NOT NULL,
    nome_arquivo_storage VARCHAR(255) NOT NULL,
    tipo_conteudo        VARCHAR(100) NOT NULL,
    tamanho_bytes        BIGINT       NOT NULL,
    caminho_storage      VARCHAR(500) NOT NULL,
    data_upload          TIMESTAMP    NOT NULL,

    CONSTRAINT pk_anexos PRIMARY KEY (id),
    CONSTRAINT uk_anexos_nome_storage UNIQUE (nome_arquivo_storage),
    CONSTRAINT fk_anexos_ordem FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico (id)
);
