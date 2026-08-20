-- Tabela de Usuários (Autenticação)
CREATE TABLE tb_usuario (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(100) NOT NULL UNIQUE,
                            password VARCHAR(255) NOT NULL,
                            role VARCHAR(20) NOT NULL,
                            ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabela de Pacientes
CREATE TABLE tb_paciente (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             nome VARCHAR(150) NOT NULL,
                             cpf VARCHAR(14) NOT NULL UNIQUE,
                             email VARCHAR(100) NOT NULL,
                             telefone VARCHAR(20)
);

-- Tabela de Profissionais (Médicos e Enfermeiros)
CREATE TABLE tb_profissional (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 nome VARCHAR(150) NOT NULL,
                                 registro_profissional VARCHAR(30) NOT NULL UNIQUE,
                                 tipo VARCHAR(20) NOT NULL, -- MEDICO ou ENFERMEIRO
                                 especialidade VARCHAR(100)
);

-- Tabela de Consultas
CREATE TABLE tb_consulta (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             paciente_id BIGINT NOT NULL,
                             profissional_id BIGINT NOT NULL,
                             data_hora TIMESTAMP NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
                             observacoes TEXT,
                             CONSTRAINT fk_consulta_paciente FOREIGN KEY (paciente_id) REFERENCES tb_paciente(id),
                             CONSTRAINT fk_consulta_profissional FOREIGN KEY (profissional_id) REFERENCES tb_profissional(id)
);