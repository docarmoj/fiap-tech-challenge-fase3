-- Schema base do CareHub. Sintaxe PostgreSQL (em teste roda no H2 com MODE=PostgreSQL).

-- Pacientes
CREATE TABLE tb_paciente (
    id       BIGSERIAL PRIMARY KEY,
    nome     VARCHAR(150) NOT NULL,
    cpf      VARCHAR(14)  NOT NULL UNIQUE,
    email    VARCHAR(150) NOT NULL UNIQUE,
    telefone VARCHAR(20)
);

-- Profissionais (medicos e enfermeiros)
CREATE TABLE tb_profissional (
    id                     BIGSERIAL PRIMARY KEY,
    nome                   VARCHAR(150) NOT NULL,
    registro_profissional  VARCHAR(30)  NOT NULL UNIQUE,
    tipo                   VARCHAR(20)  NOT NULL, -- MEDICO ou ENFERMEIRO
    especialidade          VARCHAR(100)
);

-- Usuarios de autenticacao. paciente_id/profissional_id ligam a credencial ao dominio.
CREATE TABLE tb_usuario (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    paciente_id     BIGINT UNIQUE,
    profissional_id BIGINT UNIQUE,
    CONSTRAINT fk_usuario_paciente     FOREIGN KEY (paciente_id)     REFERENCES tb_paciente(id),
    CONSTRAINT fk_usuario_profissional FOREIGN KEY (profissional_id) REFERENCES tb_profissional(id),
    -- A role tem que combinar com o vinculo, senao a autorizacao le um id nulo
    CONSTRAINT ck_usuario_role_vinculo CHECK (
        (role = 'PACIENTE' AND paciente_id IS NOT NULL AND profissional_id IS NULL)
     OR (role IN ('MEDICO', 'ENFERMEIRO') AND profissional_id IS NOT NULL AND paciente_id IS NULL)
    )
);

-- Consultas
CREATE TABLE tb_consulta (
    id              BIGSERIAL PRIMARY KEY,
    paciente_id     BIGINT      NOT NULL,
    profissional_id BIGINT      NOT NULL,
    data_hora       TIMESTAMP   NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
    observacoes     TEXT,
    CONSTRAINT fk_consulta_paciente     FOREIGN KEY (paciente_id)     REFERENCES tb_paciente(id),
    CONSTRAINT fk_consulta_profissional FOREIGN KEY (profissional_id) REFERENCES tb_profissional(id)
);

-- O Postgres nao indexa coluna de FK sozinho
CREATE INDEX idx_consulta_paciente     ON tb_consulta (paciente_id);
CREATE INDEX idx_consulta_profissional ON tb_consulta (profissional_id);
CREATE INDEX idx_consulta_data_hora    ON tb_consulta (data_hora);
