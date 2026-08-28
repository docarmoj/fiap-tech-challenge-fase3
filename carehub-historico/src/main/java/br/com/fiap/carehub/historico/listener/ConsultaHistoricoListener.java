package br.com.fiap.carehub.historico.listener;

import br.com.fiap.carehub.historico.dto.ConsultaEvent;
import br.com.fiap.carehub.historico.service.ConsultaHistoricoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// O parametro tipado e o que faz o converter usar o tipo local em vez do header __TypeId__,
// que carrega o nome da classe do carehub-agendamento e nao existe neste classpath.
@Component
public class ConsultaHistoricoListener {

    private final ConsultaHistoricoService consultaHistoricoService;

    public ConsultaHistoricoListener(ConsultaHistoricoService consultaHistoricoService) {
        this.consultaHistoricoService = consultaHistoricoService;
    }

    @RabbitListener(queues = "${carehub.rabbitmq.queue}")
    public void receberEvento(ConsultaEvent event) {
        consultaHistoricoService.registrar(event);
    }
}
