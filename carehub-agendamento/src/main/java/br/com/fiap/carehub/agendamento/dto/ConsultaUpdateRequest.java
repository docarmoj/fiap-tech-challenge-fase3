package br.com.fiap.carehub.agendamento.dto;

import br.com.fiap.carehub.agendamento.enums.StatusConsulta;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaUpdateRequest {

    private Long pacienteId;
    private Long profissionalId;
    private LocalDateTime dataHora;
    private StatusConsulta status;
    private String observacoes;
}