package br.com.fiap.carehub.agendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaRequest {

    @NotNull(message = "pacienteId é obrigatório")
    private Long pacienteId;

    @NotNull(message = "profissionalId é obrigatório")
    private Long profissionalId;

    @NotNull(message = "dataHora é obrigatória")
    @Future(message = "dataHora deve estar no futuro")
    private LocalDateTime dataHora;

    @Size(max = 1000, message = "observacoes deve ter no máximo 1000 caracteres")
    private String observacoes;
}
