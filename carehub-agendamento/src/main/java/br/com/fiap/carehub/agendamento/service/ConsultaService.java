package br.com.fiap.carehub.agendamento.service;

import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.repository.ConsultaRepository;
import br.com.fiap.carehub.agendamento.repository.PacienteRepository;
import br.com.fiap.carehub.agendamento.repository.ProfissionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import br.com.fiap.carehub.agendamento.dto.ConsultaRequest;
import br.com.fiap.carehub.agendamento.model.Paciente;
import br.com.fiap.carehub.agendamento.model.Profissional;
import br.com.fiap.carehub.agendamento.dto.ConsultaUpdateRequest;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository
    ) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
    }

    public Consulta buscarPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Consulta não encontrada"
                ));
    }

    public Consulta criarConsulta(ConsultaRequest request) {

        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paciente não encontrado"
                ));

        Profissional profissional = profissionalRepository.findById(request.getProfissionalId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"
                ));

        Consulta consulta = Consulta.builder()
                .paciente(paciente)
                .profissional(profissional)
                .dataHora(request.getDataHora())
                .observacoes(request.getObservacoes())
                .build();

        return consultaRepository.save(consulta);
    }

    public Consulta atualizarConsulta(Long id, ConsultaUpdateRequest request) {

        Consulta consulta = buscarPorId(id);

        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paciente não encontrado"
                ));

        Profissional profissional = profissionalRepository.findById(request.getProfissionalId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Profissional não encontrado"
                ));

        consulta.setPaciente(paciente);
        consulta.setProfissional(profissional);
        consulta.setDataHora(request.getDataHora());
        consulta.setStatus(request.getStatus());
        consulta.setObservacoes(request.getObservacoes());

        Consulta consultaAtualizada = consultaRepository.save(consulta);

        return buscarPorId(consultaAtualizada.getId());
    }

}
