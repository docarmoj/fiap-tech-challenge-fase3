package br.com.fiap.carehub.agendamento.repository;

import br.com.fiap.carehub.agendamento.model.Consulta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    @Override
    @EntityGraph(attributePaths = {"paciente", "profissional"})
    Optional<Consulta> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"paciente", "profissional"})
    List<Consulta> findAll();

    @EntityGraph(attributePaths = {"paciente", "profissional"})
    List<Consulta> findByPacienteId(Long pacienteId);
}
