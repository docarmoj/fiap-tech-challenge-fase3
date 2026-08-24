package br.com.fiap.carehub.agendamento.dto;

import br.com.fiap.carehub.agendamento.enums.StatusConsulta;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsultaResponse {

    private Long id;

    private Long pacienteId;
    private String pacienteNome;

    private Long profissionalId;
    private String profissionalNome;

    private LocalDateTime dataHora;
    private StatusConsulta status;
    private String observacoes;
}