package br.com.fiap.carehub.historico;

import br.com.fiap.carehub.historico.dto.ConsultaEvent;
import br.com.fiap.carehub.historico.enums.StatusConsulta;
import br.com.fiap.carehub.historico.listener.ConsultaHistoricoListener;
import br.com.fiap.carehub.historico.model.ConsultaHistorico;
import br.com.fiap.carehub.historico.repository.ConsultaHistoricoRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// O listener e exercitado chamando o metodo diretamente, que e o que o broker faria.
// Nenhum broker sobe na suite.
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Consumo do evento de consulta")
public class ConsultaHistoricoListenerTest {

    @Autowired
    private ConsultaHistoricoListener listener;

    @Autowired
    private ConsultaHistoricoRepository repository;

    private ConsultaEvent evento(Long consultaId, String status, String acao, LocalDateTime ocorridoEm) {
        return new ConsultaEvent(
                consultaId,
                1L,
                "Joao Silva",
                "joao.silva@email.com",
                1L,
                "Dr. Carlos Eduardo",
                LocalDateTime.of(2031, 4, 12, 11, 0),
                status,
                "Observacao do evento",
                acao,
                ocorridoEm
        );
    }

    @Test
    void deveGravarConsultaNovaAPartirDoEvento() {

        listener.receberEvento(evento(99L, "AGENDADA", "CONSULTA_CRIADA",
                LocalDateTime.of(2026, 8, 28, 10, 0)));

        ConsultaHistorico gravada = repository.findByConsultaId(99L).orElseThrow();

        assertThat(gravada.getPacienteId()).isEqualTo(1L);
        assertThat(gravada.getPacienteNome()).isEqualTo("Joao Silva");
        assertThat(gravada.getProfissionalId()).isEqualTo(1L);
        assertThat(gravada.getProfissionalNome()).isEqualTo("Dr. Carlos Eduardo");
        assertThat(gravada.getDataHora()).isEqualTo(LocalDateTime.of(2031, 4, 12, 11, 0));
        assertThat(gravada.getStatus()).isEqualTo(StatusConsulta.AGENDADA);
        assertThat(gravada.getObservacoes()).isEqualTo("Observacao do evento");
        assertThat(gravada.getAtualizadoEm()).isEqualTo(LocalDateTime.of(2026, 8, 28, 10, 0));
    }

    @Test
    void deveAtualizarLinhaVindaDaCargaInicialSemDuplicar() {

        long linhasAntes = repository.count();

        listener.receberEvento(evento(1L, "CANCELADA", "CONSULTA_ALTERADA",
                LocalDateTime.of(2026, 8, 28, 10, 0)));

        assertThat(repository.count()).isEqualTo(linhasAntes);

        ConsultaHistorico atualizada = repository.findByConsultaId(1L).orElseThrow();

        assertThat(atualizada.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
        assertThat(atualizada.getObservacoes()).isEqualTo("Observacao do evento");
    }

    @Test
    void oMesmoEventoEntregueDuasVezesDeixaUmaLinha() {

        ConsultaEvent event = evento(98L, "AGENDADA", "CONSULTA_CRIADA",
                LocalDateTime.of(2026, 8, 28, 10, 0));

        listener.receberEvento(event);
        long linhasAposPrimeira = repository.count();

        listener.receberEvento(event);

        assertThat(repository.count()).isEqualTo(linhasAposPrimeira);
        assertThat(repository.findByConsultaId(98L)).isPresent();
    }

    @Test
    void deveDescartarEventoMaisAntigoQueOEstadoGravado() {

        listener.receberEvento(evento(97L, "REALIZADA", "CONSULTA_ALTERADA",
                LocalDateTime.of(2026, 8, 28, 12, 0)));

        listener.receberEvento(evento(97L, "CANCELADA", "CONSULTA_ALTERADA",
                LocalDateTime.of(2026, 8, 28, 9, 0)));

        ConsultaHistorico gravada = repository.findByConsultaId(97L).orElseThrow();

        assertThat(gravada.getStatus()).isEqualTo(StatusConsulta.REALIZADA);
        assertThat(gravada.getAtualizadoEm()).isEqualTo(LocalDateTime.of(2026, 8, 28, 12, 0));
    }
}
