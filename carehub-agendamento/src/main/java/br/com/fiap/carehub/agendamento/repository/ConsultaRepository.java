package br.com.fiap.carehub.agendamento.repository;

import br.com.fiap.carehub.agendamento.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
}