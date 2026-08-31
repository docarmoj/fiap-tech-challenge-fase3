package br.com.fiap.carehub.historico.repository;

import br.com.fiap.carehub.historico.enums.StatusConsulta;
import br.com.fiap.carehub.historico.model.ConsultaHistorico;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaHistoricoRepository extends JpaRepository<ConsultaHistorico, Long> {

    Optional<ConsultaHistorico> findByConsultaId(Long consultaId);

    List<ConsultaHistorico> findByPacienteIdOrderByDataHoraDesc(Long pacienteId);

    List<ConsultaHistorico> findByPacienteIdAndStatusOrderByDataHoraDesc(
            Long pacienteId, StatusConsulta status);

    List<ConsultaHistorico> findByPacienteIdAndDataHoraAfterOrderByDataHoraAsc(
            Long pacienteId, LocalDateTime dataHora);

    List<ConsultaHistorico> findByPacienteIdAndDataHoraAfterAndStatusOrderByDataHoraAsc(
            Long pacienteId, LocalDateTime dataHora, StatusConsulta status);
}
