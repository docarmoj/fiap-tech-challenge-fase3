package br.com.fiap.carehub.historico.controller;

import br.com.fiap.carehub.historico.dto.ConsultaResponse;
import br.com.fiap.carehub.historico.enums.StatusConsulta;
import br.com.fiap.carehub.historico.mapper.ConsultaHistoricoMapper;
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
    private final ConsultaHistoricoMapper consultaHistoricoMapper;

    public HistoricoGraphQlController(ConsultaHistoricoService consultaHistoricoService,
            AutorizacaoService autorizacaoService,
            ConsultaHistoricoMapper consultaHistoricoMapper) {
        this.consultaHistoricoService = consultaHistoricoService;
        this.autorizacaoService = autorizacaoService;
        this.consultaHistoricoMapper = consultaHistoricoMapper;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<ConsultaResponse> historicoPorPaciente(
            @Argument Long pacienteId,
            @Argument StatusConsulta status,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        autorizacaoService.validarAcessoHistorico(pacienteId, usuario);

        return consultaHistoricoMapper.toResponseList(
                consultaHistoricoService.buscarHistorico(pacienteId, status));
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")
    public List<ConsultaResponse> consultasFuturasPorPaciente(
            @Argument Long pacienteId,
            @Argument StatusConsulta status,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        autorizacaoService.validarAcessoHistorico(pacienteId, usuario);

        return consultaHistoricoMapper.toResponseList(
                consultaHistoricoService.buscarFuturas(pacienteId, status));
    }
}
