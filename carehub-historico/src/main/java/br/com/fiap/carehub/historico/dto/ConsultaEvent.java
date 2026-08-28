package br.com.fiap.carehub.historico.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

// Duplicado de proposito em relacao ao publisher do carehub-agendamento: cada consumidor
// declara so o que consome, e o produtor evolui o evento sem release coordenado.
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
