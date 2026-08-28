-- Schema do carehub-historico. Banco proprio (carehub_historico_db), independente do
-- carehub-agendamento. Sintaxe PostgreSQL (em teste roda no H2 com MODE=PostgreSQL).

-- Read model desnormalizado: uma linha por consulta, alimentada por evento.
-- Os nomes de paciente e profissional sao gravados junto, entao a leitura e um SELECT unico
-- e o historico nao precisa sincronizar cadastro de paciente nem de profissional.
CREATE TABLE tb_consulta_historico (
    id                 BIGSERIAL PRIMARY KEY,
    -- Chave natural vinda do agendamento. UNIQUE porque o consumo e idempotente.
    consulta_id        BIGINT       NOT NULL UNIQUE,
    paciente_id        BIGINT       NOT NULL,
    paciente_nome      VARCHAR(150) NOT NULL,
    profissional_id    BIGINT       NOT NULL,
    profissional_nome  VARCHAR(150) NOT NULL,
    data_hora          TIMESTAMP    NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    observacoes        TEXT,
    -- Instante do evento que produziu esta versao da linha. Evento mais antigo e descartado.
    atualizado_em      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_consulta_historico_paciente  ON tb_consulta_historico (paciente_id);
CREATE INDEX idx_consulta_historico_data_hora ON tb_consulta_historico (data_hora);

-- Credenciais proprias do servico de historico. Nao ha FK para paciente ou profissional:
-- este banco nao replica esses cadastros, so guarda o vinculo que a autorizacao le.
CREATE TABLE tb_usuario (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    paciente_id     BIGINT UNIQUE,
    profissional_id BIGINT UNIQUE,
    -- A role tem que combinar com o vinculo, senao a autorizacao le um id nulo
    CONSTRAINT ck_usuario_role_vinculo CHECK (
        (role = 'PACIENTE' AND paciente_id IS NOT NULL AND profissional_id IS NULL)
     OR (role IN ('MEDICO', 'ENFERMEIRO') AND profissional_id IS NOT NULL AND paciente_id IS NULL)
    )
);
