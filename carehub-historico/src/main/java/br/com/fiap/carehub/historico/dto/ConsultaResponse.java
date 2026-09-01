package br.com.fiap.carehub.historico.dto;

import br.com.fiap.carehub.historico.enums.StatusConsulta;
import java.time.LocalDateTime;

public record ConsultaResponse(
        Long id,
        LocalDateTime dataHora,
        StatusConsulta status,
        String observacoes,
        PacienteResponse paciente,
        ProfissionalResponse profissional
) {
}
