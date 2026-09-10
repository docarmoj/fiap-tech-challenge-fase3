package br.com.fiap.carehub.historico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.carehub.historico.enums.StatusConsulta;
import br.com.fiap.carehub.historico.model.ConsultaHistorico;
import br.com.fiap.carehub.historico.repository.ConsultaHistoricoRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Testcontainers
class HistoricoRabbitMqGraphQlIT {

    private static final long CONSULTA_ID = 902L;

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.13-management-alpine"));

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ConsultaHistoricoRepository consultaHistoricoRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void eventoRabbitMqAtualizaHistoricoEGraphQlRespeitaOPerfilDoPaciente() throws Exception {
        publicarEvento("AGENDADA", "CONSULTA_CRIADA", LocalDateTime.of(2026, 9, 6, 10, 0));
        aguardarStatus(StatusConsulta.AGENDADA);

        publicarEvento("CANCELADA", "CONSULTA_ALTERADA", LocalDateTime.of(2026, 9, 6, 10, 1));
        ConsultaHistorico atualizada = aguardarStatus(StatusConsulta.CANCELADA);

        assertThat(atualizada.getPacienteId()).isEqualTo(1L);
        assertThat(atualizada.getObservacoes()).isEqualTo("Evento de integração CANCELADA");

        executarGraphQl(
                "query { historicoPorPaciente(pacienteId: \"1\") { id status } }",
                "medico1")
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.data.historicoPorPaciente[?(@.id == '902')].status",
                        hasItem("CANCELADA")));

        executarGraphQl(
                "query { historicoPorPaciente(pacienteId: \"1\") { id } }",
                "paciente1")
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.data.historicoPorPaciente[?(@.id == '902')].id",
                        hasItem("902")));

        executarGraphQl(
                "query { historicoPorPaciente(pacienteId: \"1\") { id } }",
                "paciente2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.historicoPorPaciente").doesNotExist())
                .andExpect(jsonPath("$.errors[0].extensions.classification").value("FORBIDDEN"));
    }

    private void publicarEvento(String status, String acao, LocalDateTime ocorridoEm) {
        String eventoPublicadoPeloAgendamento = """
                {
                  "consultaId": 902,
                  "pacienteId": 1,
                  "nomePaciente": "Joao Silva",
                  "emailPaciente": "joao.silva@email.com",
                  "profissionalId": 1,
                  "nomeProfissional": "Dr. Carlos Eduardo",
                  "dataHora": "2099-12-10T14:00:00",
                  "status": "%s",
                  "observacoes": "Evento de integração %s",
                  "acao": "%s",
                  "ocorridoEm": "%s"
                }
                """.formatted(status, status, acao, ocorridoEm);

        MessageProperties propriedades = new MessageProperties();
        propriedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        propriedades.setHeader("__TypeId__", "br.com.fiap.carehub.agendamento.dto.ConsultaEvent");
        Message mensagem = new Message(
                eventoPublicadoPeloAgendamento.getBytes(StandardCharsets.UTF_8),
                propriedades);

        rabbitTemplate.send(
                "carehub.consultas.exchange",
                "consulta.evento",
                mensagem
        );
    }

    private ConsultaHistorico aguardarStatus(StatusConsulta statusEsperado) throws InterruptedException {
        long limite = System.currentTimeMillis() + 10_000;

        while (System.currentTimeMillis() < limite) {
            Optional<ConsultaHistorico> consulta = consultaHistoricoRepository.findByConsultaId(CONSULTA_ID);
            if (consulta.isPresent() && consulta.get().getStatus() == statusEsperado) {
                return consulta.get();
            }
            Thread.sleep(100);
        }

        throw new AssertionError("O histórico não recebeu o status " + statusEsperado);
    }

    private ResultActions executarGraphQl(String query, String username) throws Exception {
        String corpo = "{\"query\":" + quote(query) + "}";

        return mockMvc.perform(post("/graphql")
                .with(httpBasic(username, "123456"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private String quote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n") + "\"";
    }
}
