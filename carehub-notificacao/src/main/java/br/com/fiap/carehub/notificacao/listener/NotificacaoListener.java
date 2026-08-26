package br.com.fiap.carehub.notificacao.listener;

import br.com.fiap.carehub.notificacao.dto.ConsultaEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoListener {

	@RabbitListener(queues = "${carehub.rabbitmq.queue}")
	public void receberLembrete(ConsultaEvent event) {
		/*Implementar lógica de envio de notificação */
		System.out.println("--------------------------------------------------");
		System.out.println("[NOTIFICAÇÃO] Lembrete para: " + event.nomePaciente());
		System.out.println("E-mail: " + event.emailPaciente());
		System.out.println("Consulta #" + event.consultaId() + " (" + event.acao() + ")");
		System.out.println("Data: " + event.dataHora());
		System.out.println("--------------------------------------------------");
	}
}