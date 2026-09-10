package br.com.fiap.carehub.agendamento.controller;

import br.com.fiap.carehub.agendamento.dto.ConsultaRequest;
import br.com.fiap.carehub.agendamento.dto.ConsultaResponse;
import br.com.fiap.carehub.agendamento.dto.ConsultaUpdateRequest;
import br.com.fiap.carehub.agendamento.mapper.ConsultaMapper;
import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.security.AutorizacaoService;
import br.com.fiap.carehub.agendamento.security.UsuarioAutenticado;
import br.com.fiap.carehub.agendamento.service.ConsultaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final AutorizacaoService autorizacaoService;
    private final ConsultaMapper consultaMapper;

    public ConsultaController(ConsultaService consultaService, AutorizacaoService autorizacaoService,
            ConsultaMapper consultaMapper) {
        this.consultaService = consultaService;
        this.autorizacaoService = autorizacaoService;
        this.consultaMapper = consultaMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<ConsultaResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {

        return consultaMapper.toResponseList(autorizacaoService.filtroDeListagem(usuario)
                .map(consultaService::listarPorPaciente)
                .orElseGet(consultaService::listarTodas));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ConsultaResponse buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Consulta consulta = consultaService.buscarPorId(id);

        autorizacaoService.validarAcessoConsulta(consulta, usuario);

        return consultaMapper.toResponse(consulta);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ConsultaResponse criarConsulta(@Valid @RequestBody ConsultaRequest request) {

        return consultaMapper.toResponse(consultaService.criarConsulta(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ConsultaResponse atualizarConsulta(
            @PathVariable Long id,
            @Valid @RequestBody ConsultaUpdateRequest request) {

        return consultaMapper.toResponse(consultaService.atualizarConsulta(id, request));
    }
}
