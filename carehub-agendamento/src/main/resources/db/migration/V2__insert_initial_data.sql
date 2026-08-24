-- Carga inicial. Senha de todos os usuarios: 123456 (BCrypt com prefixo {bcrypt}).
-- Os ids sao resolvidos por chave natural para nao depender do valor da sequence.

INSERT INTO tb_paciente (nome, cpf, email, telefone) VALUES
    ('Joao Silva',   '123.456.789-00', 'joao.silva@email.com',   '(11) 99999-8888'),
    ('Maria Souza',  '987.654.321-00', 'maria.souza@email.com',  '(11) 97777-6666'),
    ('Pedro Almeida','111.222.333-44', 'pedro.almeida@email.com','(11) 95555-4444');

INSERT INTO tb_profissional (nome, registro_profissional, tipo, especialidade) VALUES
    ('Dr. Carlos Eduardo', 'CRM/SP 123456',   'MEDICO',     'Cardiologia'),
    ('Enf. Ana Maria',     'COREN/SP 654321', 'ENFERMEIRO', 'Triagem Hospitalar');

-- Um usuario por identidade de dominio
INSERT INTO tb_usuario (username, password, role, ativo, paciente_id, profissional_id) VALUES
    ('medico1', '{bcrypt}$2a$10$PIDQJG3cXg5L3FZUWVieCuCXLrtRyytkCvVnGYlHMvyyE0OhGTwje', 'MEDICO', TRUE,
        NULL, (SELECT id FROM tb_profissional WHERE registro_profissional = 'CRM/SP 123456')),
    ('enfermeiro1', '{bcrypt}$2a$10$XCl6ty46Ooe7TAKMQ8ZjXeXVrdhjH1RywhDScSvcIg5zlbMFmUnle', 'ENFERMEIRO', TRUE,
        NULL, (SELECT id FROM tb_profissional WHERE registro_profissional = 'COREN/SP 654321')),
    ('paciente1', '{bcrypt}$2a$10$gt./9PlxJ0TQAIKJdm3IYe90Ps7vuoI9Y6uA8z6EUIebbDrB6ZzkS', 'PACIENTE', TRUE,
        (SELECT id FROM tb_paciente WHERE cpf = '123.456.789-00'), NULL),
    ('paciente2', '{bcrypt}$2a$10$Erga/nOZH4riSwwEQkkaNuJZgj8pry9lnllzz9Jv.NUP57FiuPUEG', 'PACIENTE', TRUE,
        (SELECT id FROM tb_paciente WHERE cpf = '987.654.321-00'), NULL),
    ('inativo1', '{bcrypt}$2a$10$02MrF18/lDJVXamdGuLdCudjfhAkS/4G4qiTpa2v.VTJ1ADZQSyyy', 'PACIENTE', FALSE,
        (SELECT id FROM tb_paciente WHERE cpf = '111.222.333-44'), NULL);

-- Uma consulta passada e uma futura por paciente, para as queries da DEV TASK 07
INSERT INTO tb_consulta (paciente_id, profissional_id, data_hora, status, observacoes) VALUES
    ((SELECT id FROM tb_paciente WHERE cpf = '123.456.789-00'),
     (SELECT id FROM tb_profissional WHERE registro_profissional = 'CRM/SP 123456'),
     '2025-03-10 09:00:00', 'REALIZADA', 'Consulta de rotina - retorno em 6 meses'),
    ((SELECT id FROM tb_paciente WHERE cpf = '123.456.789-00'),
     (SELECT id FROM tb_profissional WHERE registro_profissional = 'CRM/SP 123456'),
     '2030-09-15 10:00:00', 'AGENDADA', 'Consulta de rotina'),
    ((SELECT id FROM tb_paciente WHERE cpf = '987.654.321-00'),
     (SELECT id FROM tb_profissional WHERE registro_profissional = 'COREN/SP 654321'),
     '2025-05-02 14:30:00', 'REALIZADA', 'Triagem inicial'),
    ((SELECT id FROM tb_paciente WHERE cpf = '987.654.321-00'),
     (SELECT id FROM tb_profissional WHERE registro_profissional = 'CRM/SP 123456'),
     '2030-11-20 08:00:00', 'AGENDADA', 'Avaliacao cardiologica');
