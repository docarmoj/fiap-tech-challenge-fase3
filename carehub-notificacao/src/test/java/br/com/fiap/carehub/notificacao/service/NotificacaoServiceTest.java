package br.com.fiap.carehub.notificacao.service;

import br.com.fiap.carehub.notificacao.dto.ConsultaEvent;
import br.com.fiap.carehub.notificacao.dto.LembreteNotificacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificacaoServiceTest {

    private final NotificacaoService notificacaoService =
            new NotificacaoService();

    @Test
    void deveGerarLembreteParaConsultaCriada() {

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

        LembreteNotificacao lembrete =
                notificacaoService.gerarLembrete(event);

        assertEquals(10L, lembrete.consultaId());
        assertEquals(1L, lembrete.pacienteId());
        assertEquals("Joao Silva", lembrete.nomePaciente());
        assertEquals("joao.silva@email.com", lembrete.emailPaciente());
        assertEquals(dataHora, lembrete.dataHora());
        assertEquals("Consulta agendada", lembrete.assunto());
        assertTrue(lembrete.mensagem().contains("Joao Silva"));
        assertTrue(lembrete.mensagem().contains("10"));
        assertTrue(lembrete.mensagem().contains(dataHora.toString()));
    }

    @Test
    void deveGerarLembreteParaConsultaAlterada() {

        LocalDateTime dataHora =
                LocalDateTime.of(2031, 1, 21, 16, 0);

        ConsultaEvent event = new ConsultaEvent(
                10L,
                1L,
                "Joao Silva",
                "joao.silva@email.com",
                dataHora,
                "CONSULTA_ALTERADA"
        );

        LembreteNotificacao lembrete =
                notificacaoService.gerarLembrete(event);

        assertEquals(10L, lembrete.consultaId());
        assertEquals(1L, lembrete.pacienteId());
        assertEquals("Consulta alterada", lembrete.assunto());
        assertTrue(lembrete.mensagem().contains("foi alterada"));
        assertTrue(lembrete.mensagem().contains(dataHora.toString()));
    }
}