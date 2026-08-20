INSERT INTO tb_usuario (username, password, role, ativo) VALUES
                                                             ('medico1', '{noop}123456', 'ROLE_MEDICO', true),
                                                             ('enfermeiro1', '{noop}123456', 'ROLE_ENFERMEIRO', true),
                                                             ('paciente1', '{noop}123456', 'ROLE_PACIENTE', true);

-- Inserir Profissionais
INSERT INTO tb_profissional (nome, registro_profissional, tipo, especialidade) VALUES
                                                                                   ('Dr. Carlos Eduardo', 'CRM/SP 123456', 'MEDICO', 'Cardiologia'),
                                                                                   ('Enf. Ana Maria', 'COREN/SP 654321', 'ENFERMEIRO', 'Triagem Hospitalar');

-- Inserir Paciente
INSERT INTO tb_paciente (nome, cpf, email, telefone) VALUES
    ('João Silva', '123.456.789-00', 'joao.silva@email.com', '(11) 99999-8888');

-- Inserir uma Consulta Inicial
INSERT INTO tb_consulta (paciente_id, profissional_id, data_hora, status, observacoes) VALUES
    (1, 1, '2026-09-15 10:00:00', 'AGENDADA', 'Consulta de rotina');