package br.com.fiap.carehub.notificacao.service;

import br.com.fiap.carehub.notificacao.dto.ConsultaEvent;
import br.com.fiap.carehub.notificacao.dto.LembreteNotificacao;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoService {

    public LembreteNotificacao gerarLembrete(ConsultaEvent event) {

        String assunto;
        String mensagem;

        if ("CONSULTA_ALTERADA".equals(event.acao())) {
            assunto = "Consulta alterada";
            mensagem = "Olá, " + event.nomePaciente()
                    + ". Sua consulta #" + event.consultaId()
                    + " foi alterada. Nova data/horário: "
                    + event.dataHora() + ".";
        } else {
            assunto = "Consulta agendada";
            mensagem = "Olá, " + event.nomePaciente()
                    + ". Sua consulta #" + event.consultaId()
                    + " está agendada para "
                    + event.dataHora() + ".";
        }

        return new LembreteNotificacao(
                event.consultaId(),
                event.pacienteId(),
                event.nomePaciente(),
                event.emailPaciente(),
                event.dataHora(),
                assunto,
                mensagem
        );
    }
}