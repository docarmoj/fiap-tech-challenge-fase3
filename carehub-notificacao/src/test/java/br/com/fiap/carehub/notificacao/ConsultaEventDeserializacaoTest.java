package br.com.fiap.carehub.notificacao;

import br.com.fiap.carehub.notificacao.dto.ConsultaEvent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

// O evento e publicado pelo carehub-agendamento com mais campos do que este servico consome.
// O DTO daqui declara so o que o listener usa; este teste prova que isso nao quebra.
// O tipo vem do parametro do @RabbitListener (tipo inferido), e nao do header __TypeId__,
// que carrega o nome da classe do agendamento e nao existe neste classpath.
@DisplayName("Desserializacao do evento de consulta")
public class ConsultaEventDeserializacaoTest {

    @Test
    void deveIgnorarCamposQueEsteServicoNaoConsome() {

        String json = """
                {
                  "consultaId": 1,
                  "pacienteId": 2,
                  "nomePaciente": "Joao Silva",
                  "emailPaciente": "joao.silva@email.com",
                  "profissionalId": 3,
                  "nomeProfissional": "Dr. Carlos Eduardo",
                  "dataHora": "2030-09-15T10:00:00",
                  "status": "AGENDADA",
                  "observacoes": "Consulta de rotina",
                  "acao": "CONSULTA_CRIADA",
                  "ocorridoEm": "2026-08-28T09:30:00"
                }
                """;

        MessageProperties propriedades = new MessageProperties();
        propriedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        propriedades.setHeader("__TypeId__", "br.com.fiap.carehub.agendamento.dto.ConsultaEvent");
        propriedades.setInferredArgumentType(ConsultaEvent.class);

        Message mensagem = new Message(json.getBytes(StandardCharsets.UTF_8), propriedades);

        Object convertido = new JacksonJsonMessageConverter().fromMessage(mensagem);

        assertThat(convertido).isInstanceOf(ConsultaEvent.class);

        ConsultaEvent event = (ConsultaEvent) convertido;

        assertThat(event.consultaId()).isEqualTo(1L);
        assertThat(event.pacienteId()).isEqualTo(2L);
        assertThat(event.nomePaciente()).isEqualTo("Joao Silva");
        assertThat(event.emailPaciente()).isEqualTo("joao.silva@email.com");
        assertThat(event.dataHora()).isEqualTo(LocalDateTime.of(2030, 9, 15, 10, 0));
        assertThat(event.acao()).isEqualTo("CONSULTA_CRIADA");
    }
}
