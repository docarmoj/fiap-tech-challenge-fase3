package br.com.fiap.carehub.historico.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

// Copia do evento publicado pelo carehub-agendamento.
public record ConsultaEvent(
        Long consultaId,
        Long pacienteId,
        String nomePaciente,
        String emailPaciente,
        Long profissionalId,
        String nomeProfissional,
        LocalDateTime dataHora,
        String status,
        String observacoes,
        String acao,
        LocalDateTime ocorridoEm
) implements Serializable {
}
