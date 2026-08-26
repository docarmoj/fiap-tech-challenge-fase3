package br.com.fiap.carehub.agendamento.messaging;

import br.com.fiap.carehub.agendamento.dto.ConsultaEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsultaEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public ConsultaEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${carehub.rabbitmq.exchange}") String exchange,
            @Value("${carehub.rabbitmq.routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publicar(ConsultaEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
