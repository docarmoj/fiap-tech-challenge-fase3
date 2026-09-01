-- Schema do carehub-historico, no banco proprio. Sintaxe PostgreSQL.

-- Uma linha por consulta, preenchida pelos eventos do agendamento.
-- Os nomes ficam na propria linha para a consulta ler so esta tabela.
CREATE TABLE tb_consulta_historico (
    id                 BIGSERIAL PRIMARY KEY,
    -- Id da consulta no agendamento. UNIQUE para o mesmo evento nao duplicar a linha.
    consulta_id        BIGINT       NOT NULL UNIQUE,
    paciente_id        BIGINT       NOT NULL,
    paciente_nome      VARCHAR(150) NOT NULL,
    profissional_id    BIGINT       NOT NULL,
    profissional_nome  VARCHAR(150) NOT NULL,
    data_hora          TIMESTAMP    NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    observacoes        TEXT,
    -- Data do evento que atualizou a linha. Evento mais antigo e descartado.
    atualizado_em      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_consulta_historico_paciente  ON tb_consulta_historico (paciente_id);
CREATE INDEX idx_consulta_historico_data_hora ON tb_consulta_historico (data_hora);

-- Usuarios do servico de historico. Sem FK: este banco nao tem as tabelas
-- de paciente e de profissional.
CREATE TABLE tb_usuario (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    paciente_id     BIGINT UNIQUE,
    profissional_id BIGINT UNIQUE,
    -- A role tem que combinar com o vinculo
    CONSTRAINT ck_usuario_role_vinculo CHECK (
        (role = 'PACIENTE' AND paciente_id IS NOT NULL AND profissional_id IS NULL)
     OR (role IN ('MEDICO', 'ENFERMEIRO') AND profissional_id IS NOT NULL AND paciente_id IS NULL)
    )
);
