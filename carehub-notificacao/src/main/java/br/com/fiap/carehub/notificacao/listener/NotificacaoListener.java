package br.com.fiap.carehub.notificacao.listener;

import br.com.fiap.carehub.notificacao.dto.ConsultaEvent;
import br.com.fiap.carehub.notificacao.dto.LembreteNotificacao;
import br.com.fiap.carehub.notificacao.sender.NotificacaoSender;
import br.com.fiap.carehub.notificacao.service.NotificacaoService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoListener {

	private final NotificacaoService notificacaoService;
	private final NotificacaoSender notificacaoSender;

	public NotificacaoListener(
			NotificacaoService notificacaoService,
			NotificacaoSender notificacaoSender
	) {
		this.notificacaoService = notificacaoService;
		this.notificacaoSender = notificacaoSender;
	}

	@RabbitListener(queues = "${carehub.rabbitmq.queue}")
	public void receberLembrete(ConsultaEvent event) {

		LembreteNotificacao lembrete =
				notificacaoService.gerarLembrete(event);

		notificacaoSender.enviar(lembrete);
	}
}