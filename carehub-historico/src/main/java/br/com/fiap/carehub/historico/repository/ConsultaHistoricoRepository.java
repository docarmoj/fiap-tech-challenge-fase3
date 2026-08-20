package br.com.fiap.carehub.historico.repository;

import br.com.fiap.carehub.historico.model.ConsultaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaHistoricoRepository extends JpaRepository<ConsultaHistorico, Long> {

	List<ConsultaHistorico> findByPacienteId(Long pacienteId);

	List<ConsultaHistorico> findByPacienteIdAndDataHoraAfter(Long pacienteId, LocalDateTime dataHora);
}