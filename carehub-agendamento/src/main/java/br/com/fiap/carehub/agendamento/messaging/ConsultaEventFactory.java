package br.com.fiap.carehub.agendamento.messaging;

import br.com.fiap.carehub.agendamento.dto.ConsultaEvent;
import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.model.Paciente;
import br.com.fiap.carehub.agendamento.model.Profissional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class ConsultaEventFactory {

    public ConsultaEvent criarEventoConsultaCriada(Consulta consulta) {
        return criarEvento(consulta, "CONSULTA_CRIADA");
    }

    public ConsultaEvent criarEventoConsultaAlterada(Consulta consulta) {
        return criarEvento(consulta, "CONSULTA_ALTERADA");
    }

    private ConsultaEvent criarEvento(Consulta consulta, String acao) {
        Paciente paciente = consulta.getPaciente();
        Profissional profissional = consulta.getProfissional();

        return new ConsultaEvent(
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
    }
}
