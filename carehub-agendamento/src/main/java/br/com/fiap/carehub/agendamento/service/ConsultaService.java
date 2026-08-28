package br.com.fiap.carehub.agendamento.service;

import br.com.fiap.carehub.agendamento.dto.ConsultaEvent;
import br.com.fiap.carehub.agendamento.dto.ConsultaRequest;
import br.com.fiap.carehub.agendamento.dto.ConsultaUpdateRequest;
import br.com.fiap.carehub.agendamento.messaging.ConsultaEventPublisher;
import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.model.Paciente;
import br.com.fiap.carehub.agendamento.model.Profissional;
import br.com.fiap.carehub.agendamento.repository.ConsultaRepository;
import br.com.fiap.carehub.agendamento.repository.PacienteRepository;
import br.com.fiap.carehub.agendamento.repository.ProfissionalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ConsultaEventPublisher consultaEventPublisher;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            ConsultaEventPublisher consultaEventPublisher
    ) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.consultaEventPublisher = consultaEventPublisher;
    }

    public List<Consulta> listarTodas() {
        return consultaRepository.findAll();
    }

    public List<Consulta> listarPorPaciente(Long pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId);
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

        Consulta consultaCriada = consultaRepository.save(consulta);

        publicarEvento(consultaCriada, "CONSULTA_CRIADA");

        return consultaCriada;
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

        Consulta consultaCarregada = buscarPorId(consultaAtualizada.getId());

        publicarEvento(consultaCarregada, "CONSULTA_ALTERADA");

        return consultaCarregada;
    }

    private void publicarEvento(Consulta consulta, String acao) {

        Paciente paciente = consulta.getPaciente();
        Profissional profissional = consulta.getProfissional();

        ConsultaEvent event = new ConsultaEvent(
                consulta.getId(),
                paciente.getId(),
                paciente.getNome(),
                paciente.getEmail(),
                profissional.getId(),
                profissional.getNome(),
                consulta.getDataHora(),
                consulta.getStatus().name(),
                consulta.getObservacoes(),
                acao,
                LocalDateTime.now()
        );

        consultaEventPublisher.publicar(event);
    }
}
