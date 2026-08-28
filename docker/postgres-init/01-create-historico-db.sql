-- Roda uma unica vez, na primeira inicializacao do container Postgres.
-- O carehub-historico e dono deste database e roda o proprio Flyway nele;
-- o carehub_db continua exclusivo do carehub-agendamento.
CREATE DATABASE carehub_historico_db;
