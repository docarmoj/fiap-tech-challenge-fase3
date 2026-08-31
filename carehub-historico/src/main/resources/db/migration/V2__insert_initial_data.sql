-- Carga inicial do historico. Espelha a carga do carehub-agendamento, porque o seed de la
-- foi inserido direto no banco e nunca passou pelo RabbitMQ: sem isto, o historico so teria
-- dado depois da primeira consulta criada com os servicos no ar.

-- Mesmas credenciais do agendamento. Senha de todos: 123456 (BCrypt com prefixo {bcrypt}).
-- ATENCAO: paciente_id e profissional_id sao literais porque este banco nao tem os cadastros
-- para resolver por chave natural, como o V2 do agendamento faz. Os valores abaixo so batem
-- enquanto as sequences de la produzirem 1..n nesta ordem. Mexer na ordem do seed do
-- agendamento, ou inserir um paciente antes dele, desalinha os dois lados: a credencial
-- passa a apontar para outro paciente e o vinculo autoriza o historico errado.
INSERT INTO tb_usuario (username, password, role, ativo, paciente_id, profissional_id) VALUES
    ('medico1', '{bcrypt}$2a$10$PIDQJG3cXg5L3FZUWVieCuCXLrtRyytkCvVnGYlHMvyyE0OhGTwje', 'MEDICO', TRUE, NULL, 1),
    ('enfermeiro1', '{bcrypt}$2a$10$XCl6ty46Ooe7TAKMQ8ZjXeXVrdhjH1RywhDScSvcIg5zlbMFmUnle', 'ENFERMEIRO', TRUE, NULL, 2),
    ('paciente1', '{bcrypt}$2a$10$gt./9PlxJ0TQAIKJdm3IYe90Ps7vuoI9Y6uA8z6EUIebbDrB6ZzkS', 'PACIENTE', TRUE, 1, NULL),
    ('paciente2', '{bcrypt}$2a$10$Erga/nOZH4riSwwEQkkaNuJZgj8pry9lnllzz9Jv.NUP57FiuPUEG', 'PACIENTE', TRUE, 2, NULL),
    ('inativo1', '{bcrypt}$2a$10$02MrF18/lDJVXamdGuLdCudjfhAkS/4G4qiTpa2v.VTJ1ADZQSyyy', 'PACIENTE', FALSE, 3, NULL);

-- Uma consulta passada e uma futura por paciente. Os consulta_id sao os mesmos do agendamento,
-- entao um evento sobre qualquer uma delas atualiza a linha em vez de inserir outra.
-- atualizado_em fica no passado de proposito: qualquer evento real supera a carga.
INSERT INTO tb_consulta_historico
    (consulta_id, paciente_id, paciente_nome, profissional_id, profissional_nome, data_hora, status, observacoes, atualizado_em) VALUES
    (1, 1, 'Joao Silva',  1, 'Dr. Carlos Eduardo', '2025-03-10 09:00:00', 'REALIZADA', 'Consulta de rotina - retorno em 6 meses', '2025-01-01 00:00:00'),
    (2, 1, 'Joao Silva',  1, 'Dr. Carlos Eduardo', '2030-09-15 10:00:00', 'AGENDADA',  'Consulta de rotina',                      '2025-01-01 00:00:00'),
    (3, 2, 'Maria Souza', 2, 'Enf. Ana Maria',     '2025-05-02 14:30:00', 'REALIZADA', 'Triagem inicial',                         '2025-01-01 00:00:00'),
    (4, 2, 'Maria Souza', 1, 'Dr. Carlos Eduardo', '2030-11-20 08:00:00', 'AGENDADA',  'Avaliacao cardiologica',                  '2025-01-01 00:00:00');
