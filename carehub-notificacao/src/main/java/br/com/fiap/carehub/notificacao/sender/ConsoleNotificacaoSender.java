package br.com.fiap.carehub.notificacao.sender;

import br.com.fiap.carehub.notificacao.dto.LembreteNotificacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleNotificacaoSender implements NotificacaoSender {

    private static final Logger logger =
            LoggerFactory.getLogger(ConsoleNotificacaoSender.class);

    @Override
    public void enviar(LembreteNotificacao lembrete) {

        logger.info(
                """

                --------------------------------------------------
                [LEMBRETE]
                Paciente: {}
                E-mail: {}
                Consulta: #{}
                Data/hora: {}
                Assunto: {}
                Mensagem: {}
                --------------------------------------------------
                """,
                lembrete.nomePaciente(),
                lembrete.emailPaciente(),
                lembrete.consultaId(),
                lembrete.dataHora(),
                lembrete.assunto(),
                lembrete.mensagem()
        );
    }
}