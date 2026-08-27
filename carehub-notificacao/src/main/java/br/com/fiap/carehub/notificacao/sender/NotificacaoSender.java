package br.com.fiap.carehub.notificacao.sender;

import br.com.fiap.carehub.notificacao.dto.LembreteNotificacao;

public interface NotificacaoSender {

    void enviar(LembreteNotificacao lembrete);
}