package br.com.fiap.carehub.notificacao.config;

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
    public Queue notificacoesQueue(
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
    public Queue notificacoesDlq(
            @Value("${carehub.rabbitmq.dlq}") String dlqName
    ) {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding notificacoesBinding(
            Queue notificacoesQueue,
            DirectExchange consultasExchange,
            @Value("${carehub.rabbitmq.routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(notificacoesQueue).to(consultasExchange).with(routingKey);
    }

    @Bean
    public Binding notificacoesDlqBinding(
            Queue notificacoesDlq,
            DirectExchange consultasDlx,
            @Value("${carehub.rabbitmq.dlq-routing-key}") String dlqRoutingKey
    ) {
        return BindingBuilder.bind(notificacoesDlq).to(consultasDlx).with(dlqRoutingKey);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
