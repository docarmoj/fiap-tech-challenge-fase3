package br.com.fiap.carehub.notificacao.dto;

import java.time.LocalDateTime;

public record LembreteNotificacao(
        Long consultaId,
        Long pacienteId,
        String nomePaciente,
        String emailPaciente,
        LocalDateTime dataHora,
        String assunto,
        String mensagem
) {
}