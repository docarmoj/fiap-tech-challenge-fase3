package br.com.fiap.carehub.agendamento.controller;

import br.com.fiap.carehub.agendamento.dto.ConsultaResponse;
import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.service.ConsultaService;
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

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping("/{id}")
    public ConsultaResponse buscarPorId(@PathVariable Long id) {

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
    public ConsultaResponse criarConsulta(@RequestBody ConsultaRequest request) {

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
    public ConsultaResponse atualizarConsulta(
            @PathVariable Long id,
            @RequestBody ConsultaUpdateRequest request
    ) {

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