package br.com.fiap.carehub.historico.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// A exchange e direct e a fila do historico usa a mesma routing key da fila de notificacao:
// as duas filas recebem cada evento publicado pelo agendamento.
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange consultasExchange(
            @Value("${carehub.rabbitmq.exchange}") String exchangeName
    ) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange consultasDlx(
            @Value("${carehub.rabbitmq.dlx}") String dlxName
    ) {
        return new DirectExchange(dlxName, true, false);
    }

    @Bean
    public Queue historicoQueue(
            @Value("${carehub.rabbitmq.queue}") String queueName,
            @Value("${carehub.rabbitmq.dlx}") String dlxName,
            @Value("${carehub.rabbitmq.dlq-routing-key}") String dlqRoutingKey
    ) {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(dlxName)
                .deadLetterRoutingKey(dlqRoutingKey)
                .build();
    }

    @Bean
    public Queue historicoDlq(
            @Value("${carehub.rabbitmq.dlq}") String dlqName
    ) {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding historicoBinding(
            Queue historicoQueue,
            DirectExchange consultasExchange,
            @Value("${carehub.rabbitmq.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(historicoQueue).to(consultasExchange).with(routingKey);
    }

    @Bean
    public Binding historicoDlqBinding(
            Queue historicoDlq,
            DirectExchange consultasDlx,
            @Value("${carehub.rabbitmq.dlq-routing-key}") String dlqRoutingKey
    ) {
        return BindingBuilder.bind(historicoDlq).to(consultasDlx).with(dlqRoutingKey);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
