package br.com.fiap.carehub.agendamento.controller;

import br.com.fiap.carehub.agendamento.dto.ConsultaResponse;
import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.service.ConsultaService;
import br.com.fiap.carehub.agendamento.security.AutorizacaoService;
import br.com.fiap.carehub.agendamento.security.UsuarioAutenticado;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.fiap.carehub.agendamento.dto.ConsultaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import br.com.fiap.carehub.agendamento.dto.ConsultaUpdateRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final AutorizacaoService autorizacaoService;

    public ConsultaController(ConsultaService consultaService, AutorizacaoService autorizacaoService) {
        this.consultaService = consultaService;
        this.autorizacaoService = autorizacaoService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ConsultaResponse buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        autorizacaoService.validarAcessoConsulta(id, usuario);

        Consulta consulta = consultaService.buscarPorId(id);

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public ConsultaResponse criarConsulta(
            @RequestBody ConsultaRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        autorizacaoService.validarCriacaoConsulta(request.getPacienteId(), usuario);

        Consulta consulta = consultaService.criarConsulta(request);

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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ConsultaResponse atualizarConsulta(
            @PathVariable Long id,
            @RequestBody ConsultaUpdateRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        autorizacaoService.validarAtualizacaoConsulta(id, usuario);

        Consulta consulta = consultaService.atualizarConsulta(id, request);

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