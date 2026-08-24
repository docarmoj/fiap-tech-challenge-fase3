package br.com.fiap.carehub.agendamento.repository;

import br.com.fiap.carehub.agendamento.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {
}
