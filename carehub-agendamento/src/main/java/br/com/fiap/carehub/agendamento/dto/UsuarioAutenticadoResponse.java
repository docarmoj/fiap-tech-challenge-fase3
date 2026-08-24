package br.com.fiap.carehub.agendamento.dto;

import br.com.fiap.carehub.agendamento.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

// Retorno de GET /usuarios/me
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioAutenticadoResponse(
        String username,
        Role role,
        Long pacienteId,
        Long profissionalId) {
}
