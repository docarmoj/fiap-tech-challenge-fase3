package br.com.fiap.carehub.agendamento.repository;

import br.com.fiap.carehub.agendamento.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
