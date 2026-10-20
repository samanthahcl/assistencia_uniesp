CREATE TABLE tecnicos
(
    id           BIGSERIAL    NOT NULL,
    nome         VARCHAR(150) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    telefone     VARCHAR(20),
    especialidade VARCHAR(100),
    ativo        BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_tecnicos PRIMARY KEY (id),
    CONSTRAINT uk_tecnicos_email UNIQUE (email)
);

CREATE TABLE pecas
(
    id                 BIGSERIAL      NOT NULL,
    nome               VARCHAR(150)   NOT NULL,
    codigo             VARCHAR(50)    NOT NULL,
    preco              NUMERIC(10, 2) NOT NULL,
    quantidade_estoque INTEGER        NOT NULL DEFAULT 0,
    ativo              BOOLEAN        NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_pecas PRIMARY KEY (id),
    CONSTRAINT uk_pecas_codigo UNIQUE (codigo)
);

CREATE TABLE usuarios
(
    id           BIGSERIAL    NOT NULL,
    nome         VARCHAR(150) NOT NULL,
    email        VARCHAR(150) NOT NULL,
    senha        VARCHAR(255) NOT NULL,
    perfil       VARCHAR(20)  NOT NULL,
    ativo        BOOLEAN      NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP    NOT NULL,

    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
);
