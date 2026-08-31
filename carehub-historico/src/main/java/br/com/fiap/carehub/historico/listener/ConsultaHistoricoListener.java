package br.com.fiap.carehub.historico.listener;

import br.com.fiap.carehub.historico.dto.ConsultaEvent;
import br.com.fiap.carehub.historico.service.ConsultaHistoricoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Recebe o evento da fila e manda gravar no historico.
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
