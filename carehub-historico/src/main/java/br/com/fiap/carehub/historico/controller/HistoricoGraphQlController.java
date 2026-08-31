package br.com.fiap.carehub.historico.controller;

import br.com.fiap.carehub.historico.dto.ConsultaResponse;
import br.com.fiap.carehub.historico.dto.PacienteResponse;
import br.com.fiap.carehub.historico.dto.ProfissionalResponse;
import br.com.fiap.carehub.historico.enums.StatusConsulta;
import br.com.fiap.carehub.historico.model.ConsultaHistorico;
import br.com.fiap.carehub.historico.security.AutorizacaoService;
import br.com.fiap.carehub.historico.security.UsuarioAutenticado;
import br.com.fiap.carehub.historico.service.ConsultaHistoricoService;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class HistoricoGraphQlController {

    private final ConsultaHistoricoService consultaHistoricoService;
    private final AutorizacaoService autorizacaoService;

    public HistoricoGraphQlController(ConsultaHistoricoService consultaHistoricoService,
            AutorizacaoService autorizacaoService) {
        this.consultaHistoricoService = consultaHistoricoService;
        this.autorizacaoService = autorizacaoService;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<ConsultaResponse> historicoPorPaciente(
            @Argument Long pacienteId,
            @Argument StatusConsulta status,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        autorizacaoService.validarAcessoHistorico(pacienteId, usuario);

        return toResponse(consultaHistoricoService.buscarHistorico(pacienteId, status));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<ConsultaResponse> consultasFuturasPorPaciente(
            @Argument Long pacienteId,
            @Argument StatusConsulta status,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        autorizacaoService.validarAcessoHistorico(pacienteId, usuario);

        return toResponse(consultaHistoricoService.buscarFuturas(pacienteId, status));
    }

    private List<ConsultaResponse> toResponse(List<ConsultaHistorico> consultas) {
        return consultas.stream().map(this::toResponse).toList();
    }

    private ConsultaResponse toResponse(ConsultaHistorico consulta) {
        return new ConsultaResponse(
                consulta.getConsultaId(),
                consulta.getDataHora(),
                consulta.getStatus(),
                consulta.getObservacoes(),
                new PacienteResponse(consulta.getPacienteId(), consulta.getPacienteNome()),
                new ProfissionalResponse(consulta.getProfissionalId(), consulta.getProfissionalNome()));
    }
}
