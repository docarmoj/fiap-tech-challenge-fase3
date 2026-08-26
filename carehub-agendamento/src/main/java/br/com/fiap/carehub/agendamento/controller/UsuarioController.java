package br.com.fiap.carehub.agendamento.controller;

import br.com.fiap.carehub.agendamento.dto.UsuarioAutenticadoResponse;
import br.com.fiap.carehub.agendamento.security.UsuarioAutenticado;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ResponseEntity<UsuarioAutenticadoResponse> me(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(new UsuarioAutenticadoResponse(
                usuario.getUsername(),
                usuario.getRole(),
                usuario.getPacienteId(),
                usuario.getProfissionalId()));
    }
}
