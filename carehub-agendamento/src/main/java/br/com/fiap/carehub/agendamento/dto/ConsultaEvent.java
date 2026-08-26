package br.com.fiap.carehub.agendamento.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ConsultaEvent(
        Long consultaId,
        Long pacienteId,
        String nomePaciente,
        String emailPaciente,
        LocalDateTime dataHora,
        String acao
) implements Serializable {
}
