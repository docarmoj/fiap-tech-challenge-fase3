package br.com.fiap.carehub.agendamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.carehub.agendamento.dto.ConsultaEvent;
import br.com.fiap.carehub.agendamento.model.Consulta;
import br.com.fiap.carehub.agendamento.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Testcontainers
class AgendamentoRabbitMqIT {

    private static final String FILA_NOTIFICACOES = "carehub.notificacoes.queue";

    @Container
    @ServiceConnection
    static final RabbitMQContainer RABBITMQ = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.13-management-alpine"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void limparFila() {
        rabbitTemplate.execute(channel -> {
            channel.queuePurge(FILA_NOTIFICACOES);
            return null;
        });
    }

    @Test
    void medicoAutenticadoCriaPersisteEPublicaConsultaNoRabbitMq() throws Exception {
        String observacoes = "Consulta do teste Spring com RabbitMQ real";
        String corpo = """
                {
                  "pacienteId": 1,
                  "profissionalId": 1,
                  "dataHora": "2099-10-20T14:30:00",
                  "observacoes": "%s"
                }
                """.formatted(observacoes);

        mockMvc.perform(post("/consultas")
                        .with(httpBasic("medico1", "123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADA"))
                .andExpect(jsonPath("$.pacienteId").value(1));

        Consulta consultaPersistida = consultaRepository.findAll().stream()
                .filter(consulta -> observacoes.equals(consulta.getObservacoes()))
                .findFirst()
                .orElseThrow();

        assertThat(consultaPersistida.getId()).isNotNull();
        assertThat(consultaPersistida.getStatus().name()).isEqualTo("AGENDADA");

        Object mensagem = rabbitTemplate.receiveAndConvert(FILA_NOTIFICACOES, 10_000);

        assertThat(mensagem).isInstanceOf(ConsultaEvent.class);
        ConsultaEvent evento = (ConsultaEvent) mensagem;
        assertThat(evento.consultaId()).isEqualTo(consultaPersistida.getId());
        assertThat(evento.pacienteId()).isEqualTo(1L);
        assertThat(evento.status()).isEqualTo("AGENDADA");
        assertThat(evento.acao()).isEqualTo("CONSULTA_CRIADA");
        assertThat(evento.ocorridoEm()).isNotNull();
    }

    @Test
    void medicoAutenticadoAlteraPersisteEPublicaConsultaNoRabbitMq() throws Exception {
        String corpo = """
                {
                  "pacienteId": 1,
                  "profissionalId": 1,
                  "dataHora": "2099-11-10T09:00:00",
                  "status": "CANCELADA",
                  "observacoes": "Consulta alterada pelo teste de integração"
                }
                """;

        mockMvc.perform(put("/consultas/2")
                        .with(httpBasic("medico1", "123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        Consulta consultaPersistida = consultaRepository.findById(2L).orElseThrow();
        assertThat(consultaPersistida.getStatus().name()).isEqualTo("CANCELADA");
        assertThat(consultaPersistida.getObservacoes())
                .isEqualTo("Consulta alterada pelo teste de integração");

        Object mensagem = rabbitTemplate.receiveAndConvert(FILA_NOTIFICACOES, 10_000);

        assertThat(mensagem).isInstanceOf(ConsultaEvent.class);
        ConsultaEvent evento = (ConsultaEvent) mensagem;
        assertThat(evento.consultaId()).isEqualTo(2L);
        assertThat(evento.status()).isEqualTo("CANCELADA");
        assertThat(evento.acao()).isEqualTo("CONSULTA_ALTERADA");
    }

    @Test
    void pacienteNaoPodeCriarConsulta() throws Exception {
        String corpo = """
                {
                  "pacienteId": 1,
                  "profissionalId": 1,
                  "dataHora": "2099-10-20T14:30:00",
                  "observacoes": "Operação proibida para paciente"
                }
                """;

        mockMvc.perform(post("/consultas")
                        .with(httpBasic("paciente1", "123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }
}
