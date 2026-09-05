package br.com.fiap.carehub.agendamento.controller;

import br.com.fiap.carehub.agendamento.dto.ConsultaRequest;
import br.com.fiap.carehub.agendamento.dto.ConsultaResponse;
import br.com.fiap.carehub.agendamento.dto.ConsultaUpdateRequest;
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

    public ConsultaController(ConsultaService consultaService, AutorizacaoService autorizacaoService) {
        this.consultaService = consultaService;
        this.autorizacaoService = autorizacaoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<ConsultaResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {

        List<Consulta> consultas = autorizacaoService.filtroDeListagem(usuario)
                .map(consultaService::listarPorPaciente)
                .orElseGet(consultaService::listarTodas);

        return consultas.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ConsultaResponse buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Consulta consulta = consultaService.buscarPorId(id);

        autorizacaoService.validarAcessoConsulta(consulta, usuario);

        return toResponse(consulta);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ConsultaResponse criarConsulta(@Valid @RequestBody ConsultaRequest request) {

        return toResponse(consultaService.criarConsulta(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ConsultaResponse atualizarConsulta(
            @PathVariable Long id,
            @Valid @RequestBody ConsultaUpdateRequest request) {

        return toResponse(consultaService.atualizarConsulta(id, request));
    }

    private ConsultaResponse toResponse(Consulta consulta) {
        return ConsultaResponse.builder()
                .id(consulta.getId())
                .pacienteId(consulta.getPaciente().getId())
                .pacienteNome(consulta.getPaciente().getNome())
                .profissionalId(consulta.getProfissional().getId())
                .profissionalNome(consulta.getProfissional().getNome())
                .dataHora(consulta.getDataHora())
                .status(consulta.getStatus())
                .observacoes(consulta.getObservacoes())
                .build();
    }
}
