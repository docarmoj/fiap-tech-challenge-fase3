package br.com.fiap.carehub.notificacao.integracao;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import br.com.fiap.carehub.notificacao.sender.NotificacaoSender;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("integration")
class NotificacaoRabbitMqIT {

    private static final String EXCHANGE_CONSULTAS = "carehub.consultas.exchange";
    private static final String ROUTING_KEY_CONSULTA_EVENTO = "consulta.evento";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private NotificacaoSender notificacaoSender;

    @Test
    void eventoRabbitMqEConsumidoProcessadoEEnviadoComoLembrete() {
        LocalDateTime dataHora = LocalDateTime.of(2099, 11, 15, 9, 30);
        String eventoPublicadoPeloAgendamento = """
                {
                  "consultaId": 901,
                  "pacienteId": 1,
                  "nomePaciente": "Joao Silva",
                  "emailPaciente": "joao.silva@email.com",
                  "profissionalId": 1,
                  "nomeProfissional": "Dr. Carlos Eduardo",
                  "dataHora": "2099-11-15T09:30:00",
                  "status": "AGENDADA",
                  "observacoes": "Evento com o contrato completo do agendamento",
                  "acao": "CONSULTA_CRIADA",
                  "ocorridoEm": "2099-01-01T10:00:00"
                }
                """;

        MessageProperties propriedades = new MessageProperties();
        propriedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        propriedades.setHeader("__TypeId__", "br.com.fiap.carehub.agendamento.dto.ConsultaEvent");
        Message mensagem = new Message(
                eventoPublicadoPeloAgendamento.getBytes(StandardCharsets.UTF_8),
                propriedades);

        rabbitTemplate.send(EXCHANGE_CONSULTAS, ROUTING_KEY_CONSULTA_EVENTO, mensagem);

        verify(notificacaoSender, timeout(10_000)).enviar(argThat(lembrete ->
                lembrete != null
                        && lembrete.consultaId().equals(901L)
                        && lembrete.pacienteId().equals(1L)
                        && lembrete.dataHora().equals(dataHora)
                        && lembrete.assunto().equals("Consulta agendada")
                        && lembrete.mensagem().contains("Joao Silva")
        ));
    }
}
