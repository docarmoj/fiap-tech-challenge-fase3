package br.com.fiap.carehub.agendamento.dto;

import br.com.fiap.carehub.agendamento.enums.StatusConsulta;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultaUpdateRequest {

    @NotNull(message = "pacienteId é obrigatório")
    private Long pacienteId;

    @NotNull(message = "profissionalId é obrigatório")
    private Long profissionalId;

    @NotNull(message = "dataHora é obrigatória")
    @Future(message = "dataHora deve estar no futuro")
    private LocalDateTime dataHora;

    @NotNull(message = "status é obrigatório")
    private StatusConsulta status;

    @Size(max = 1000, message = "observacoes deve ter no máximo 1000 caracteres")
    private String observacoes;
}