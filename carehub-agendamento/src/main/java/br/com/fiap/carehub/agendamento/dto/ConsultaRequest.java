package br.com.fiap.carehub.agendamento.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaRequest {

    private Long pacienteId;
    private Long profissionalId;
    private LocalDateTime dataHora;
    private String observacoes;
}
