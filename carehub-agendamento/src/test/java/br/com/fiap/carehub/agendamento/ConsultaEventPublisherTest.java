package br.com.fiap.carehub.agendamento;

import br.com.fiap.carehub.agendamento.dto.ConsultaEvent;
import br.com.fiap.carehub.agendamento.messaging.ConsultaEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConsultaEventPublisherTest {

    @Test
    void devePublicarEventoNoRabbitMQ() {

        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);

        ConsultaEventPublisher publisher = new ConsultaEventPublisher(
                rabbitTemplate,
                "carehub.consultas.exchange",
                "consulta.notificacao"
        );

        ConsultaEvent event = new ConsultaEvent(
                1L,
                1L,
                "Paciente Teste",
                "paciente@teste.com",
                LocalDateTime.of(2026, 10, 20, 14, 30),
                "CONSULTA_CRIADA"
        );

        publisher.publicar(event);

        verify(rabbitTemplate).convertAndSend(
                "carehub.consultas.exchange",
                "consulta.notificacao",
                event
        );
    }
}