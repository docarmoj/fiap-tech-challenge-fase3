package br.com.fiap.carehub.agendamento.service;

import br.com.fiap.carehub.agendamento.dto.ConsultaRequest;
import br.com.fiap.carehub.agendamento.dto.ConsultaUpdateRequest;
import br.com.fiap.carehub.agendamento.messaging.ConsultaEventFactory;
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

import java.util.List;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ConsultaEventFactory consultaEventFactory;
    private final ConsultaEventPublisher consultaEventPublisher;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            PacienteRepository pacienteRepository,
            ProfissionalRepository profissionalRepository,
            ConsultaEventFactory consultaEventFactory,
            ConsultaEventPublisher consultaEventPublisher
    ) {
        this.consultaRepository = consultaRepository;
        this.pacienteRepository = pacienteRepository;
        this.profissionalRepository = profissionalRepository;
        this.consultaEventFactory = consultaEventFactory;
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

        Consulta consulta = Consulta.agendar(
                paciente,
                profissional,
                request.getDataHora(),
                request.getObservacoes());

        Consulta consultaCriada = consultaRepository.save(consulta);

        consultaEventPublisher.publicar(consultaEventFactory.criarEventoConsultaCriada(consultaCriada));

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

        consulta.atualizarAgendamento(
                paciente,
                profissional,
                request.getDataHora(),
                request.getStatus(),
                request.getObservacoes());

        Consulta consultaAtualizada = consultaRepository.save(consulta);

        Consulta consultaCarregada = buscarPorId(consultaAtualizada.getId());

        consultaEventPublisher.publicar(consultaEventFactory.criarEventoConsultaAlterada(consultaCarregada));

        return consultaCarregada;
    }
}
