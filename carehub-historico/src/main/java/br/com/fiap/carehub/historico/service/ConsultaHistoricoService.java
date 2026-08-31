package br.com.fiap.carehub.historico.service;

import br.com.fiap.carehub.historico.dto.ConsultaEvent;
import br.com.fiap.carehub.historico.enums.StatusConsulta;
import br.com.fiap.carehub.historico.model.ConsultaHistorico;
import br.com.fiap.carehub.historico.repository.ConsultaHistoricoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaHistoricoService {

    private final ConsultaHistoricoRepository consultaHistoricoRepository;

    public ConsultaHistoricoService(ConsultaHistoricoRepository consultaHistoricoRepository) {
        this.consultaHistoricoRepository = consultaHistoricoRepository;
    }

    // Atualiza a consulta se ela ja existe, senao cria. Evento antigo e ignorado.
    @Transactional
    public void registrar(ConsultaEvent event) {

        ConsultaHistorico consulta = consultaHistoricoRepository
                .findByConsultaId(event.consultaId())
                .orElseGet(() -> ConsultaHistorico.builder()
                        .consultaId(event.consultaId())
                        .build());

        if (consulta.getAtualizadoEm() != null
                && !event.ocorridoEm().isAfter(consulta.getAtualizadoEm())) {
            return;
        }

        consulta.setPacienteId(event.pacienteId());
        consulta.setPacienteNome(event.nomePaciente());
        consulta.setProfissionalId(event.profissionalId());
        consulta.setProfissionalNome(event.nomeProfissional());
        consulta.setDataHora(event.dataHora());
        consulta.setStatus(StatusConsulta.valueOf(event.status()));
        consulta.setObservacoes(event.observacoes());
        consulta.setAtualizadoEm(event.ocorridoEm());

        consultaHistoricoRepository.save(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaHistorico> buscarHistorico(Long pacienteId, StatusConsulta status) {

        return status == null
                ? consultaHistoricoRepository.findByPacienteIdOrderByDataHoraDesc(pacienteId)
                : consultaHistoricoRepository
                        .findByPacienteIdAndStatusOrderByDataHoraDesc(pacienteId, status);
    }

    @Transactional(readOnly = true)
    public List<ConsultaHistorico> buscarFuturas(Long pacienteId, StatusConsulta status) {

        LocalDateTime agora = LocalDateTime.now();

        return status == null
                ? consultaHistoricoRepository
                        .findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(pacienteId, agora)
                : consultaHistoricoRepository
                        .findByPacienteIdAndDataHoraAfterAndStatusOrderByDataHoraAsc(
                                pacienteId, agora, status);
    }
}
