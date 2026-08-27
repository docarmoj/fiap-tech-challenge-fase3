package br.com.fiap.carehub.notificacao.listener;

import br.com.fiap.carehub.notificacao.dto.ConsultaEvent;
import br.com.fiap.carehub.notificacao.dto.LembreteNotificacao;
import br.com.fiap.carehub.notificacao.sender.NotificacaoSender;
import br.com.fiap.carehub.notificacao.service.NotificacaoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoListenerTest {

    @Test
    void deveProcessarEventoRecebidoEEnviarLembrete() {

        NotificacaoService notificacaoService =
                mock(NotificacaoService.class);

        NotificacaoSender notificacaoSender =
                mock(NotificacaoSender.class);

        NotificacaoListener listener =
                new NotificacaoListener(
                        notificacaoService,
                        notificacaoSender
                );

        LocalDateTime dataHora =
                LocalDateTime.of(2031, 1, 20, 14, 30);

        ConsultaEvent event = new ConsultaEvent(
                10L,
                1L,
                "Joao Silva",
                "joao.silva@email.com",
                dataHora,
                "CONSULTA_CRIADA"
        );

        LembreteNotificacao lembrete = new LembreteNotificacao(
                10L,
                1L,
                "Joao Silva",
                "joao.silva@email.com",
                dataHora,
                "Consulta agendada",
                "Sua consulta foi agendada."
        );

        when(notificacaoService.gerarLembrete(event))
                .thenReturn(lembrete);

        listener.receberLembrete(event);

        verify(notificacaoService)
                .gerarLembrete(event);

        verify(notificacaoSender)
                .enviar(lembrete);
    }
}